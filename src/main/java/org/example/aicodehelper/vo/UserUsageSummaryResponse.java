package org.example.aicodehelper.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(name = "UserUsageSummaryResponse", description = "用户调用统计摘要")
public class UserUsageSummaryResponse {
    @Schema(description = "累计请求次数", example = "31")
    private long totalRequests;

    @Schema(description = "累计输入字符数", example = "4280")
    private long totalPromptChars;

    @Schema(description = "累计输出字符数", example = "16240")
    private long totalCompletionChars;

    @Schema(description = "最近调用记录")
    private List<RecentCallItem> recentCalls;

    @Data
    @Builder
    @Schema(name = "RecentCallItem", description = "单次调用记录")
    public static class RecentCallItem {
        @Schema(description = "模型名称", example = "qwen-max")
        private String modelName;

        @Schema(description = "会话 ID", example = "12")
        private long memoryId;

        @Schema(description = "会话标题", example = "AI 学习路线")
        private String conversationTitle;

        @Schema(description = "输入字符数", example = "56")
        private int requestLength;

        @Schema(description = "输出字符数", example = "632")
        private int responseLength;

        @Schema(description = "耗时毫秒", example = "1530")
        private long latencyMs;

        @Schema(description = "是否成功", example = "true")
        private boolean success;

        @Schema(description = "错误信息，成功时为空", example = "null")
        private String errorMessage;

        @Schema(description = "创建时间", example = "2026-03-24 14:20:01")
        private String createdAt;
    }
}
