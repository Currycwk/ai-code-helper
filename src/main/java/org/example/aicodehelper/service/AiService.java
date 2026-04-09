package org.example.aicodehelper.service;

import lombok.RequiredArgsConstructor;
import org.example.aicodehelper.domain.ConversationSession;
import org.example.aicodehelper.domain.UserAccount;
import org.example.aicodehelper.event.SseChatEvent;
import org.example.aicodehelper.event.SseChatMeta;
import org.example.aicodehelper.event.SseTokenUsage;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

/**
 * AI 对话应用服务。
 * 负责串联用户校验、配额校验、会话消息落库、SSE 事件组装与调用统计，
 */
@Service
@RequiredArgsConstructor
public class AiService {

    private final ChatFacadeService chatFacadeService;
    private final UserAccountService userAccountService;
    private final UsageService usageService;
    private final ConversationService conversationService;

    /**
     * 处理普通聊天请求：
     * 先拿到当前用户和模型信息，完成配额校验，
     * 再把用户消息写入会话，最后启动流式回复。
     */
    public Flux<ServerSentEvent<SseChatEvent>> chat(Long userId, Long conversationId, String message) {
        UserAccount user = userAccountService.getRequiredById(userId);
        String modelName = chatFacadeService.resolveModel(user);
        chatFacadeService.ensureQuota(user, modelName);
        ConversationSession conversation = conversationService.getOwnedConversation(user, conversationId);
        conversationService.appendUserMessage(conversation, message);
        return streamChat(user, conversation, conversationId, modelName, message);
    }

    /**
     * 处理“编辑用户消息后重新生成”：
     * 会先替换原消息，并删除它之后的旧分支消息，
     * 然后基于新的输入重新发起一次流式回复。
     */
    public Flux<ServerSentEvent<SseChatEvent>> regenerate(Long userId,
                                                          Long conversationId,
                                                          Long messageId,
                                                          String message) {
        UserAccount user = userAccountService.getRequiredById(userId);
        String modelName = chatFacadeService.resolveModel(user);
        chatFacadeService.ensureQuota(user, modelName);
        ConversationSession conversation = conversationService.editUserMessage(user, conversationId, messageId, message);
        return streamChat(user, conversation, conversationId, modelName, message);
    }

    /**
     * 统一封装聊天流的处理过程。
     * 这里把“接收模型流式输出”“拼接完整回复”“收尾落库”“异常兜底”放在一起，
     * 这样 chat 和 regenerate 可以共用同一套编排逻辑。
     */
    private Flux<ServerSentEvent<SseChatEvent>> streamChat(UserAccount user,
                                                           ConversationSession conversation,
                                                           Long conversationId,
                                                           String modelName,
                                                           String message) {
        // 记录开始时间，后面用于计算本次请求耗时。
        long startNanos = System.nanoTime();
        // 一边向前端推送增量内容，一边把完整回答拼接起来，供最终落库和统计使用。
        StringBuilder responseBuilder = new StringBuilder();

        Flux<ServerSentEvent<SseChatEvent>> stream = chatFacadeService.chatStream(user, conversationId, message)
                // 模型每返回一个文本分片，都先追加到完整结果中。
                .doOnNext(responseBuilder::append)
                // 再把原始文本分片包装成 SSE 的 delta 事件返回给前端。
                .map(this::buildDeltaEvent);

        // done 事件里会带上一些元信息，比如模型名和耗时。
        SseChatMeta meta = buildMeta(modelName);

        return stream
                // 流正常结束后，补发一个 done 事件，并执行成功后的持久化与调用记录。
                .concatWith(Mono.fromSupplier(() -> buildDoneEvent(user,
                        conversation,
                        conversationId,
                        modelName,
                        message,
                        responseBuilder,
                        meta,
                        startNanos)))
                // 流执行中如果出现异常，也统一转成 SSE error 事件返回，并记录失败信息。
                .onErrorResume(error -> Flux.just(buildErrorEvent(user,
                        conversation,
                        conversationId,
                        modelName,
                        message,
                        responseBuilder,
                        startNanos,
                        error)));
    }

    /**
     * 构造 done 事件需要携带的元信息对象。
     */
    private SseChatMeta buildMeta(String modelName) {
        SseChatMeta meta = new SseChatMeta();
        meta.setModel(modelName);
        meta.setTokens(new SseTokenUsage());
        return meta;
    }

    /**
     * 把模型返回的单个文本分片包装成 SSE delta 事件。
     */
    private ServerSentEvent<SseChatEvent> buildDeltaEvent(String chunk) {
        return ServerSentEvent.<SseChatEvent>builder()
                .data(new SseChatEvent("delta", chunk, null, null))
                .build();
    }

    /**
     * 正常结束时的收尾逻辑：
     * 更新耗时、保存 assistant 回复、记录调用日志，并返回 done 事件。
     */
    private ServerSentEvent<SseChatEvent> buildDoneEvent(UserAccount user,
                                                         ConversationSession conversation,
                                                         Long conversationId,
                                                         String modelName,
                                                         String message,
                                                         StringBuilder responseBuilder,
                                                         SseChatMeta meta,
                                                         long startNanos) {
        long elapsedMs = elapsedMillis(startNanos);
        meta.setElapsedMs(elapsedMs);
        String responseText = responseBuilder.toString();
        // 把完整回答作为 assistant 消息写回会话，保证后续查看历史消息时能读到。
        conversationService.appendAssistantMessage(conversation, modelName, responseText, null);
        // 记录本次成功调用的请求、响应长度与耗时等统计信息。
        usageService.record(user, modelName, conversationId, message, responseText, elapsedMs, true, null);
        return ServerSentEvent.<SseChatEvent>builder()
                .data(new SseChatEvent("done", null, null, meta))
                .build();
    }

    /**
     * 异常场景下的收尾逻辑：
     * 尽可能保留已经生成的内容，同时把错误信息落库和返回给前端。
     */
    private ServerSentEvent<SseChatEvent> buildErrorEvent(UserAccount user,
                                                          ConversationSession conversation,
                                                          Long conversationId,
                                                          String modelName,
                                                          String message,
                                                          StringBuilder responseBuilder,
                                                          long startNanos,
                                                          Throwable error) {
        String errorMessage = error.getMessage();
        long elapsedMs = elapsedMillis(startNanos);
        String responseText = responseBuilder.toString();
        // 如果模型在报错前已经输出了部分内容，就保留这些内容；否则落一条带错误标记的消息。
        String persistedContent = responseBuilder.isEmpty() ? "[错误] " + errorMessage : responseText;
        conversationService.appendAssistantMessage(conversation, modelName, persistedContent, errorMessage);
        // 失败调用也要记录，方便后续排查问题和统计失败率。
        usageService.record(user, modelName, conversationId, message, responseText, elapsedMs, false, errorMessage);
        return ServerSentEvent.<SseChatEvent>builder()
                .data(new SseChatEvent("error", null, errorMessage, null))
                .build();
    }

    /**
     * 把纳秒级开始时间换算成毫秒耗时，便于接口返回和日志统计。
     */
    private long elapsedMillis(long startNanos) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
    }
}
