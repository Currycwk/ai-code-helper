package org.example.aicodehelper.service;

import lombok.RequiredArgsConstructor;
import org.example.aicodehelper.config.AppModelCatalogProperties;
import org.example.aicodehelper.vo.ModelSummaryResponse;
import org.example.aicodehelper.exception.BadRequestException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ModelCatalogService {

    private final AppModelCatalogProperties properties;

    public List<ModelSummaryResponse> listAvailableModels() {
        return properties.getCatalog().stream()
                .map(model -> ModelSummaryResponse.builder()
                        .name(model.getName())
                        .supportsSearch(Boolean.TRUE.equals(model.getEnableSearch()))
                        .isDefault(model.getName().equals(properties.getDefaultModel()))
                        .build())
                .toList();
    }

    public AppModelCatalogProperties.ModelDefinition getRequiredModel(String modelName) {
        return properties.getCatalog().stream()
                .filter(item -> item.getName().equals(modelName))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Unsupported model: " + modelName));
    }

    public String resolvePreferredModel(String preferredModel) {
        if (preferredModel == null || preferredModel.isBlank()) {
            return properties.getDefaultModel();
        }
        getRequiredModel(preferredModel);
        return preferredModel;
    }

    public String getDefaultModel() {
        return properties.getDefaultModel();
    }
}
