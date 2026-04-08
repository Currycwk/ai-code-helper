package org.example.aicodehelper.service;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import org.example.aicodehelper.domain.ConversationMemory;
import org.example.aicodehelper.mapper.ConversationMemoryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DatabaseChatMemoryStore implements ChatMemoryStore {

    private final ConversationMemoryMapper conversationMemoryMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessage> getMessages(Object memoryId) {
        if (!(memoryId instanceof Long id)) {
            return Collections.emptyList();
        }
        ConversationMemory memory = conversationMemoryMapper.findByConversationId(id);
        if (memory == null) {
            return Collections.emptyList();
        }
        return ChatMessageDeserializer.messagesFromJson(memory.getMessagesJson());
    }

    @Override
    @Transactional
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        if (!(memoryId instanceof Long id)) {
            return;
        }
        ConversationMemory memory = conversationMemoryMapper.findByConversationId(id);
        if (memory == null) {
            memory = new ConversationMemory();
            memory.setConversationId(id);
        }
        memory.setMessagesJson(ChatMessageSerializer.messagesToJson(messages));
        memory.setUpdatedAt(LocalDateTime.now());
        if (conversationMemoryMapper.findByConversationId(id) == null) {
            conversationMemoryMapper.insert(memory);
        } else {
            conversationMemoryMapper.update(memory);
        }
    }

    @Override
    @Transactional
    public void deleteMessages(Object memoryId) {
        if (memoryId instanceof Long id) {
            conversationMemoryMapper.deleteByConversationId(id);
        }
    }
}
