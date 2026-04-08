package org.example.aicodehelper.service;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import lombok.RequiredArgsConstructor;
import org.example.aicodehelper.domain.ConversationMessage;
import org.example.aicodehelper.domain.ConversationMessageRole;
import org.example.aicodehelper.domain.ConversationSession;
import org.example.aicodehelper.domain.UserAccount;
import org.example.aicodehelper.dto.request.CreateConversationRequest;
import org.example.aicodehelper.exception.BadRequestException;
import org.example.aicodehelper.exception.NotFoundException;
import org.example.aicodehelper.mapper.ConversationMessageMapper;
import org.example.aicodehelper.mapper.ConversationSessionMapper;
import org.example.aicodehelper.vo.ConversationDetailResponse;
import org.example.aicodehelper.vo.ConversationMessageResponse;
import org.example.aicodehelper.vo.ConversationSummaryResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * 会话中心核心服务。
 * 负责管理会话和消息的完整生命周期，包括创建会话、追加消息、重命名、
 * 删除消息、编辑后重新生成，以及同步维护会话摘要和上下文记忆。
 */
@Service
@RequiredArgsConstructor
public class ConversationService {

    private static final String DEFAULT_TITLE = "新会话";
    private static final DateTimeFormatter CONVERSATION_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ConversationSessionMapper conversationSessionMapper;
    private final ConversationMessageMapper conversationMessageMapper;
    private final DatabaseChatMemoryStore databaseChatMemoryStore;

    @Transactional
    public ConversationSummaryResponse createConversation(UserAccount user, CreateConversationRequest request) {
        ConversationSession conversation = new ConversationSession();
        conversation.setUser(user);
        conversation.setManualTitle(request != null && request.getTitle() != null && !request.getTitle().isBlank());
        conversation.setTitle(normalizeTitle(request == null ? null : request.getTitle()));
        conversation.setLastMessagePreview("");
        LocalDateTime now = LocalDateTime.now();
        conversation.setCreatedAt(now);
        conversation.setUpdatedAt(now);
        conversationSessionMapper.insert(conversation);
        return toSummary(conversation, 0);
    }

    @Transactional(readOnly = true)
    public List<ConversationSummaryResponse> listConversations(UserAccount user) {
        return conversationSessionMapper.findByUserIdOrderByUpdatedAtDesc(user.getId()).stream()
                .map(conversation -> toSummary(conversation, conversationMessageMapper.countByConversationId(conversation.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public ConversationDetailResponse getConversationDetail(UserAccount user, Long conversationId) {
        ConversationSession conversation = getOwnedConversation(user, conversationId);
        List<ConversationMessageResponse> messages = conversationMessageMapper.findByConversationIdOrderByIdAsc(conversation.getId()).stream()
                .map(this::toMessageResponse)
                .toList();
        return ConversationDetailResponse.builder()
                .id(conversation.getId())
                .title(conversation.getTitle())
                .updatedAt(formatDateTime(conversation.getUpdatedAt()))
                .messages(messages)
                .build();
    }

    @Transactional(readOnly = true)
    public ConversationSession getOwnedConversation(UserAccount user, Long conversationId) {
        ConversationSession conversation = conversationSessionMapper.findByIdAndUserId(conversationId, user.getId());
        if (conversation == null) {
            throw new NotFoundException("Conversation not found");
        }
        return conversation;
    }

    @Transactional
    public ConversationSummaryResponse renameConversation(UserAccount user, Long conversationId, String title) {
        ConversationSession conversation = getOwnedConversation(user, conversationId);
        conversation.setTitle(normalizeTitle(title));
        conversation.setManualTitle(true);
        touchConversation(conversation);
        conversationSessionMapper.update(conversation);
        return toSummary(conversation, conversationMessageMapper.countByConversationId(conversation.getId()));
    }

    @Transactional
    public void appendUserMessage(ConversationSession conversation, String content) {
        ConversationMessage message = new ConversationMessage();
        message.setConversation(conversation);
        message.setRole(ConversationMessageRole.USER);
        message.setContent(normalizeContent(content));
        message.setCreatedAt(LocalDateTime.now());
        conversationMessageMapper.insert(message);
        refreshConversationMeta(conversation, message.getContent(), true);
    }

    @Transactional
    public void appendAssistantMessage(ConversationSession conversation, String modelName, String content, String errorMessage) {
        ConversationMessage message = new ConversationMessage();
        message.setConversation(conversation);
        message.setRole(ConversationMessageRole.ASSISTANT);
        message.setContent(content);
        message.setModelName(modelName);
        message.setErrorMessage(errorMessage);
        message.setCreatedAt(LocalDateTime.now());
        conversationMessageMapper.insert(message);
        refreshConversationMeta(conversation, content, false);
    }

    @Transactional
    public void clearMemory(Long conversationId) {
        databaseChatMemoryStore.deleteMessages(conversationId);
    }

    @Transactional
    public void deleteConversation(UserAccount user, Long conversationId) {
        ConversationSession conversation = getOwnedConversation(user, conversationId);
        conversationMessageMapper.deleteByConversationId(conversation.getId());
        databaseChatMemoryStore.deleteMessages(conversationId);
        conversationSessionMapper.deleteById(conversation.getId());
    }

    @Transactional
    public void deleteMessage(UserAccount user, Long conversationId, Long messageId) {
        ConversationSession conversation = getOwnedConversation(user, conversationId);
        ConversationMessage message = conversationMessageMapper.findByIdAndConversationId(messageId, conversation.getId());
        if (message == null) {
            throw new NotFoundException("Message not found");
        }
        conversationMessageMapper.deleteById(message.getId());
        rebuildConversationState(conversation);
    }

    @Transactional
    public ConversationSession editUserMessage(UserAccount user, Long conversationId, Long messageId, String newContent) {
        ConversationSession conversation = getOwnedConversation(user, conversationId);
        ConversationMessage message = conversationMessageMapper.findByIdAndConversationId(messageId, conversation.getId());
        if (message == null) {
            throw new NotFoundException("Message not found");
        }
        if (message.getRole() != ConversationMessageRole.USER) {
            throw new BadRequestException("Only user messages can be edited and regenerated");
        }
        conversationMessageMapper.deleteByConversationIdAndIdGreaterThanEqual(conversation.getId(), messageId);
        rebuildConversationState(conversation);
        appendUserMessage(conversation, newContent);
        return conversation;
    }

    private void rebuildConversationState(ConversationSession conversation) {
        List<ConversationMessage> remainingMessages = conversationMessageMapper.findByConversationIdOrderByIdAsc(conversation.getId());
        if (remainingMessages.isEmpty()) {
            if (!conversation.isManualTitle()) {
                conversation.setTitle(DEFAULT_TITLE);
            }
            conversation.setLastMessagePreview("");
            touchConversation(conversation);
            conversationSessionMapper.update(conversation);
            databaseChatMemoryStore.deleteMessages(conversation.getId());
            return;
        }

        ConversationMessage firstUserMessage = remainingMessages.stream()
                .filter(item -> item.getRole() == ConversationMessageRole.USER)
                .findFirst()
                .orElse(null);
        if (!conversation.isManualTitle() && firstUserMessage != null) {
            conversation.setTitle(generateTitle(firstUserMessage.getContent()));
        } else if (!conversation.isManualTitle()) {
            conversation.setTitle(DEFAULT_TITLE);
        }

        ConversationMessage lastMessage = remainingMessages.get(remainingMessages.size() - 1);
        conversation.setLastMessagePreview(normalizePreview(lastMessage.getContent()));
        touchConversation(conversation);
        conversationSessionMapper.update(conversation);
        rebuildMemory(conversation.getId(), remainingMessages);
    }

    private void rebuildMemory(Long conversationId, List<ConversationMessage> messages) {
        List<ChatMessage> chatMessages = messages.stream()
                .map(this::toChatMessage)
                .filter(Objects::nonNull)
                .toList();
        databaseChatMemoryStore.updateMessages(conversationId, chatMessages);
    }

    private ChatMessage toChatMessage(ConversationMessage message) {
        return switch (message.getRole()) {
            case USER -> UserMessage.from(message.getContent());
            case ASSISTANT -> AiMessage.from(message.getContent());
            case SYSTEM -> SystemMessage.from(message.getContent());
        };
    }

    private void refreshConversationMeta(ConversationSession conversation, String content, boolean isUserMessage) {
        String normalized = normalizePreview(content);
        conversation.setLastMessagePreview(normalized);
        if (isUserMessage && !conversation.isManualTitle() && isDefaultTitle(conversation.getTitle())) {
            conversation.setTitle(generateTitle(content));
        }
        touchConversation(conversation);
        conversationSessionMapper.update(conversation);
    }

    private void touchConversation(ConversationSession conversation) {
        conversation.setUpdatedAt(LocalDateTime.now());
    }

    private boolean isDefaultTitle(String title) {
        return title == null || title.isBlank() || DEFAULT_TITLE.equals(title);
    }

    private String generateTitle(String content) {
        String normalized = normalizePreview(content);
        if (normalized.isBlank()) {
            return DEFAULT_TITLE;
        }
        return normalized.length() > 24 ? normalized.substring(0, 24) + "..." : normalized;
    }

    private String normalizePreview(String content) {
        if (content == null) {
            return "";
        }
        String normalized = content.replaceAll("\\s+", " ").trim();
        return normalized.length() > 80 ? normalized.substring(0, 80) + "..." : normalized;
    }

    private String normalizeTitle(String title) {
        if (title == null || title.isBlank()) {
            return DEFAULT_TITLE;
        }
        return title.trim();
    }

    private String normalizeContent(String content) {
        if (content == null || content.isBlank()) {
            throw new BadRequestException("Message content cannot be blank");
        }
        return content.trim();
    }

    private ConversationSummaryResponse toSummary(ConversationSession conversation, long messageCount) {
        return ConversationSummaryResponse.builder()
                .id(conversation.getId())
                .title(conversation.getTitle())
                .lastMessagePreview(conversation.getLastMessagePreview())
                .messageCount(messageCount)
                .updatedAt(formatDateTime(conversation.getUpdatedAt()))
                .build();
    }

    private ConversationMessageResponse toMessageResponse(ConversationMessage message) {
        return ConversationMessageResponse.builder()
                .id(message.getId())
                .role(message.getRole())
                .content(message.getContent())
                .modelName(message.getModelName())
                .errorMessage(message.getErrorMessage())
                .createdAt(formatDateTime(message.getCreatedAt()))
                .build();
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(CONVERSATION_TIME_FORMATTER);
    }
}
