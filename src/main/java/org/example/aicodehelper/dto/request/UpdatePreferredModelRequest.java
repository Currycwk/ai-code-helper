package org.example.aicodehelper.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(name = "UpdatePreferredModelRequest", description = "修改用户默认模型请求")
public class UpdatePreferredModelRequest {
    @Schema(description = "模型名称", example = "qwen-plus")
    @NotBlank
    private String modelName;
}
