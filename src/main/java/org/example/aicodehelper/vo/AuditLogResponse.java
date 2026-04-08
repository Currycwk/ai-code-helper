package org.example.aicodehelper.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(name = "AuditLogResponse", description = "调用审计日志")
public class AuditLogResponse {

    @Schema(description = "记录 ID", example = "1001")
    private Long id;

    @Schema(description = "用户 ID", example = "2")
    private Long userId;

    @Schema(description = "用户名", example = "alice")
    private String username;

    @Schema(description = "模型名称", example = "qwen-max")
    private String modelName;

    @Schema(description = "会话 ID", example = "12")
    private Long memoryId;

    @Schema(description = "会话标题", example = "AI 学习路线")
    private String conversationTitle;

    @Schema(description = "请求预览", example = "请帮我分析这段 Java 代码")
    private String requestPreview;

    @Schema(description = "响应预览", example = "这段代码主要存在三个问题...")
    private String responsePreview;

    @Schema(description = "请求字符数", example = "64")
    private Integer requestLength;

    @Schema(description = "响应字符数", example = "320")
    private Integer responseLength;

    @Schema(description = "耗时", example = "1820")
    private Long latencyMs;

    @Schema(description = "是否成功", example = "true")
    private Boolean success;

    @Schema(description = "错误信息", example = "Daily quota exhausted")
    private String errorMessage;

    @Schema(description = "创建时间", example = "2026-03-25 15:00:01")
    private String createdAt;
}
