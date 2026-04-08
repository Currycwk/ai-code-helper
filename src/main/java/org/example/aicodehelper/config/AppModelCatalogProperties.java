package org.example.aicodehelper.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "app.models")
public class AppModelCatalogProperties {
    private String defaultModel;
    private List<ModelDefinition> catalog = new ArrayList<>();

    @Data
    public static class ModelDefinition {
        private String name;
        private String apiKey;
        private Boolean enableSearch;
        private Float temperature;
        private Integer maxTokens;
    }
}
