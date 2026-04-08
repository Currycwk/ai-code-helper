package org.example.aicodehelper.model;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import lombok.RequiredArgsConstructor;
import org.example.aicodehelper.config.AppModelCatalogProperties;
import org.example.aicodehelper.service.ModelCatalogService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class QwenChatModelConfig {

    private final AppModelCatalogProperties properties;
    private final ModelCatalogService modelCatalogService;
    private final ChatModelListener chatModelListener;

    @Bean
    public ChatModel myQwenChatModel() {
        AppModelCatalogProperties.ModelDefinition definition =
                modelCatalogService.getRequiredModel(properties.getDefaultModel());
        QwenChatModel.QwenChatModelBuilder builder = QwenChatModel.builder()
                .apiKey(definition.getApiKey())
                .modelName(definition.getName())
                .listeners(List.of(chatModelListener));
        if (definition.getEnableSearch() != null) {
            builder.enableSearch(definition.getEnableSearch());
        }
        if (definition.getTemperature() != null) {
            builder.temperature(definition.getTemperature());
        }
        if (definition.getMaxTokens() != null) {
            builder.maxTokens(definition.getMaxTokens());
        }
        return builder.build();
    }
}
