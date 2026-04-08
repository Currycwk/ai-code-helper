package org.example.aicodehelper.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(name = "ConversationSummaryResponse", description = "会话中心列表项")
public class ConversationSummaryResponse {
    @Schema(description = "会话 ID", example = "12")
    private Long id;

    @Schema(description = "会话标题", example = "Java 后端三个月学习路线")
    private String title;

    @Schema(description = "最近一条消息预览", example = "先补基础语法、集合、多线程，再学 Spring Boot...")
    private String lastMessagePreview;

    @Schema(description = "消息数量", example = "8")
    private long messageCount;

    @Schema(description = "最近更新时间", example = "2026-03-24 18:35:12")
    private String updatedAt;
}
