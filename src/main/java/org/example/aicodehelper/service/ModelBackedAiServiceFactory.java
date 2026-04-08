package org.example.aicodehelper.service;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import org.example.aicodehelper.config.AppModelCatalogProperties;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 大模型服务工厂。
 * 根据模型目录配置动态构建并缓存不同模型对应的 AI 服务实例，
 * 同时把聊天记忆、RAG 检索和流式模型能力统一装配到 LangChain4j 服务中。
 */

@Service
@RequiredArgsConstructor
public class ModelBackedAiServiceFactory {

    private final ModelCatalogService modelCatalogService;
    private final ContentRetriever contentRetriever;
    private final ChatModelListener chatModelListener;
    private final DatabaseChatMemoryStore databaseChatMemoryStore;

    private final Map<String, AiCodeHelperService> cache = new ConcurrentHashMap<>();

    public AiCodeHelperService getService(String modelName) {
        String resolved = modelCatalogService.resolvePreferredModel(modelName);
        return cache.computeIfAbsent(resolved, this::buildService);
    }

    private AiCodeHelperService buildService(String modelName) {
        AppModelCatalogProperties.ModelDefinition definition = modelCatalogService.getRequiredModel(modelName);
        QwenChatModel.QwenChatModelBuilder chatModelBuilder = QwenChatModel.builder()
                .apiKey(definition.getApiKey())
                .modelName(definition.getName())
                .listeners(List.of(chatModelListener));
        if (definition.getEnableSearch() != null) {
            chatModelBuilder.enableSearch(definition.getEnableSearch());
        }
        if (definition.getTemperature() != null) {
            chatModelBuilder.temperature(definition.getTemperature());
        }
        if (definition.getMaxTokens() != null) {
            chatModelBuilder.maxTokens(definition.getMaxTokens());
        }
        ChatModel chatModel = chatModelBuilder.build();

        QwenStreamingChatModel.QwenStreamingChatModelBuilder streamingBuilder = QwenStreamingChatModel.builder()
                .apiKey(definition.getApiKey())
                .modelName(definition.getName());
        if (definition.getEnableSearch() != null) {
            streamingBuilder.enableSearch(definition.getEnableSearch());
        }
        if (definition.getTemperature() != null) {
            streamingBuilder.temperature(definition.getTemperature());
        }
        if (definition.getMaxTokens() != null) {
            streamingBuilder.maxTokens(definition.getMaxTokens());
        }
        StreamingChatModel streamingChatModel = streamingBuilder.build();
        return AiServices.builder(AiCodeHelperService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder()
                        .id(memoryId)
                        .maxMessages(20)
                        .chatMemoryStore(databaseChatMemoryStore)
                        .build())
                .contentRetriever(contentRetriever)
                .build();
    }
}
