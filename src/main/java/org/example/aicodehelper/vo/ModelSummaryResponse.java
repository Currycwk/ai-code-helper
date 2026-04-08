package org.example.aicodehelper.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(name = "ModelSummaryResponse", description = "可选模型摘要")
public class ModelSummaryResponse {
    @Schema(description = "模型名称", example = "qwen-max")
    private String name;
    @Schema(description = "是否支持联网搜索", example = "true")
    private Boolean supportsSearch;
    @Schema(description = "是否为系统默认模型", example = "true")
    private Boolean isDefault;
}
