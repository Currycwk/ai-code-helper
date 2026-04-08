package org.example.aicodehelper.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(name = "RegenerateMessageRequest", description = "消息编辑后重新生成请求")
public class RegenerateMessageRequest {

    @NotBlank
    @Schema(description = "编辑后的用户消息", example = "把上一条回答改成一周的 Spring Boot 学习计划")
    private String message;
}
