package org.example.aicodehelper.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(name = "RenameConversationRequest", description = "会话重命名请求")
public class RenameConversationRequest {

    @NotBlank
    @Schema(description = "新的会话标题", example = "Spring Boot 面试准备")
    private String title;
}
