package org.example.aicodehelper.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.aicodehelper.domain.ConversationSession;
import org.example.aicodehelper.domain.UserAccount;
import org.example.aicodehelper.dto.request.RegenerateMessageRequest;
import org.example.aicodehelper.event.SseChatEvent;
import org.example.aicodehelper.event.SseChatMeta;
import org.example.aicodehelper.event.SseTokenUsage;
import org.example.aicodehelper.security.AppUserPrincipal;
import org.example.aicodehelper.service.ChatFacadeService;
import org.example.aicodehelper.service.ConversationService;
import org.example.aicodehelper.service.UsageService;
import org.example.aicodehelper.service.UserAccountService;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

/**
 * AI 对话控制器。
 * 负责接收前端的聊天请求，把用户输入交给大模型服务进行流式生成，
 * 并在输出过程中同步完成会话消息持久化、配额校验和调用审计记录。
 */
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "AI 对话接口")
public class AiController {

    private final ChatFacadeService chatFacadeService;
    private final UserAccountService userAccountService;
    private final UsageService usageService;
    private final ConversationService conversationService;

    @GetMapping("/chat")
    @Operation(
            summary = "SSE 流式聊天",
            description = "基于当前登录用户的默认模型进行流式对话，并将消息持久化到会话中心。"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "请求成功，开始流式返回"),
            @ApiResponse(responseCode = "401", description = "未登录", content = @Content),
            @ApiResponse(responseCode = "403", description = "超出每日配额", content = @Content),
            @ApiResponse(responseCode = "404", description = "会话不存在", content = @Content)
    })
    public Flux<ServerSentEvent<SseChatEvent>> chat(@Parameter(hidden = true) @AuthenticationPrincipal AppUserPrincipal principal,
                                                    @Parameter(description = "会话 ID；来自会话中心，继续对话时使用同一个 ID。", example = "12")
                                                    @RequestParam long conversationId,
                                                    @Parameter(description = "用户输入内容。", example = "帮我制定一份三个月 Java 后端学习路线。")
                                                    @RequestParam String message) {
        UserAccount user = userAccountService.getRequiredById(principal.getId()); //根据登录用户 ID 查完整用户对象
        String modelName = chatFacadeService.resolveModel(user); //解析用户当前应该用哪个模型
        chatFacadeService.ensureQuota(user, modelName); //校验配额
        ConversationSession conversation = conversationService.getOwnedConversation(user, conversationId); //校验这个会话是不是当前用户自己的
        conversationService.appendUserMessage(conversation, message); //先把用户消息写进会话消息表

        long startNanos = System.nanoTime(); //统计调用总耗时
        StringBuilder responseBuilder = new StringBuilder(); //用于拼接返回的文本

        Flux<ServerSentEvent<SseChatEvent>> stream = chatFacadeService.chatStream(user, conversationId, message)
                .doOnNext(responseBuilder::append)
                .map(chunk -> ServerSentEvent.<SseChatEvent>builder()
                        .data(new SseChatEvent("delta", chunk, null, null))
                        .build());

        SseChatMeta meta = new SseChatMeta();
        meta.setModel(modelName);
        meta.setTokens(new SseTokenUsage());

        ServerSentEvent<SseChatEvent> doneEvent = ServerSentEvent.<SseChatEvent>builder()
                .data(new SseChatEvent("done", null, null, meta))
                .build();

        return stream
                .concatWith(Mono.fromSupplier(() -> {
                    long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
                    meta.setElapsedMs(elapsedMs);
                    conversationService.appendAssistantMessage(conversation, modelName, responseBuilder.toString(), null);
                    usageService.record(user, modelName, conversationId, message, responseBuilder.toString(), elapsedMs, true, null);
                    return doneEvent;
                }))
                .onErrorResume(error -> {
                    String errorMessage = error.getMessage();
                    long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
                    String persistedContent = responseBuilder.isEmpty() ? "[错误] " + errorMessage : responseBuilder.toString();
                    conversationService.appendAssistantMessage(conversation, modelName, persistedContent, errorMessage);
                    usageService.record(user, modelName, conversationId, message, responseBuilder.toString(), elapsedMs, false, errorMessage);
                    return Flux.just(ServerSentEvent.<SseChatEvent>builder()
                            .data(new SseChatEvent("error", null, errorMessage, null))
                            .build());
                });
    }
    @PostMapping(value = "/conversations/{conversationId}/messages/{messageId}/regenerate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "编辑消息后重新生成", description = "替换指定的用户消息，并删除其后的消息分支，随后重新流式生成回答。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "请求成功，开始流式返回"),
            @ApiResponse(responseCode = "400", description = "请求参数非法", content = @Content),
            @ApiResponse(responseCode = "401", description = "未登录", content = @Content),
            @ApiResponse(responseCode = "403", description = "超出每日配额", content = @Content),
            @ApiResponse(responseCode = "404", description = "会话或消息不存在", content = @Content)
    })
    public Flux<ServerSentEvent<SseChatEvent>> regenerate(@Parameter(hidden = true) @AuthenticationPrincipal AppUserPrincipal principal,
                                                          @Parameter(description = "会话 ID", example = "12") @PathVariable Long conversationId,
                                                          @Parameter(description = "消息 ID", example = "88") @PathVariable Long messageId,
                                                          @Valid @RequestBody RegenerateMessageRequest request) {
        UserAccount user = userAccountService.getRequiredById(principal.getId());
        String modelName = chatFacadeService.resolveModel(user);
        chatFacadeService.ensureQuota(user, modelName);
        ConversationSession conversation = conversationService.editUserMessage(user, conversationId, messageId, request.getMessage());

        long startNanos = System.nanoTime();
        StringBuilder responseBuilder = new StringBuilder();

        Flux<ServerSentEvent<SseChatEvent>> stream = chatFacadeService.chatStream(user, conversationId, request.getMessage())
                .doOnNext(responseBuilder::append)
                .map(chunk -> ServerSentEvent.<SseChatEvent>builder()
                        .data(new SseChatEvent("delta", chunk, null, null))
                        .build());

        SseChatMeta meta = new SseChatMeta();
        meta.setModel(modelName);
        meta.setTokens(new SseTokenUsage());

        ServerSentEvent<SseChatEvent> doneEvent = ServerSentEvent.<SseChatEvent>builder()
                .data(new SseChatEvent("done", null, null, meta))
                .build();

        return stream
                .concatWith(Mono.fromSupplier(() -> {
                    long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
                    meta.setElapsedMs(elapsedMs);
                    conversationService.appendAssistantMessage(conversation, modelName, responseBuilder.toString(), null);
                    usageService.record(user, modelName, conversationId, request.getMessage(), responseBuilder.toString(), elapsedMs, true, null);
                    return doneEvent;
                }))
                .onErrorResume(error -> {
                    String errorMessage = error.getMessage();
                    long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
                    String persistedContent = responseBuilder.isEmpty() ? "[错误] " + errorMessage : responseBuilder.toString();
                    conversationService.appendAssistantMessage(conversation, modelName, persistedContent, errorMessage);
                    usageService.record(user, modelName, conversationId, request.getMessage(), responseBuilder.toString(), elapsedMs, false, errorMessage);
                    return Flux.just(ServerSentEvent.<SseChatEvent>builder()
                            .data(new SseChatEvent("error", null, errorMessage, null))
                            .build());
                });
    }
}
