package org.example.aicodehelper.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(name = "AdminDashboardResponse", description = "管理员仪表盘响应")
public class AdminDashboardResponse {
    @Schema(description = "总用户数", example = "8")
    private long totalUsers;
    @Schema(description = "今日总请求量", example = "126")
    private long todayRequests;
    @Schema(description = "今日成功请求量", example = "120")
    private long todaySuccessRequests;
    @Schema(description = "模型使用排行")
    private List<ModelUsageItem> modelUsage;

    @Data
    @Builder
    @Schema(name = "ModelUsageItem", description = "模型使用统计项")
    public static class ModelUsageItem {
        @Schema(description = "模型名称", example = "qwen-max")
        private String modelName;
        @Schema(description = "请求量", example = "78")
        private long requestCount;
    }
}
