package org.example.aicodehelper.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.example.aicodehelper.domain.ConversationMessageRole;

@Data
@Builder
@Schema(name = "ConversationMessageResponse", description = "会话中的单条消息")
public class ConversationMessageResponse {
    @Schema(description = "消息 ID", example = "21")
    private Long id;

    @Schema(description = "消息角色", example = "USER")
    private ConversationMessageRole role;

    @Schema(description = "消息文本", example = "帮我制定 Java 学习路线")
    private String content;

    @Schema(description = "模型名称，AI 回复时可能有值", example = "qwen-max")
    private String modelName;

    @Schema(description = "错误信息，正常消息为空", example = "null")
    private String errorMessage;

    @Schema(description = "创建时间", example = "2026-03-24 18:30:00")
    private String createdAt;
}
