package org.example.aicodehelper.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(name = "ConversationDetailResponse", description = "会话详情")
public class ConversationDetailResponse {
    @Schema(description = "会话 ID", example = "12")
    private Long id;

    @Schema(description = "会话标题", example = "Java 后端三个月学习路线")
    private String title;

    @Schema(description = "最近更新时间", example = "2026-03-24 18:35:12")
    private String updatedAt;

    @Schema(description = "消息列表")
    private List<ConversationMessageResponse> messages;
}
