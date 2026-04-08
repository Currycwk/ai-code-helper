package org.example.aicodehelper.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(name = "UpdateModelQuotaRequest", description = "更新用户模型配额请求")
public class UpdateModelQuotaRequest {

    @NotBlank
    @Schema(description = "模型名称", example = "qwen-max")
    private String modelName;

    @NotNull
    @Min(0)
    @Schema(description = "每日模型配额，0 表示禁用该模型", example = "30")
    private Integer dailyQuota;
}
