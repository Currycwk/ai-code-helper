package org.example.aicodehelper.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.example.aicodehelper.domain.SubscriptionTier;
import org.example.aicodehelper.domain.UserRole;

import java.util.List;

@Data
@Builder
@Schema(name = "UserProfileResponse", description = "用户资料响应")
public class UserProfileResponse {
    @Schema(description = "用户 ID", example = "1")
    private Long id;
    @Schema(description = "用户名", example = "admin")
    private String username;
    @Schema(description = "显示名称", example = "System Admin")
    private String displayName;
    @Schema(description = "角色", example = "ADMIN")
    private UserRole role;
    @Schema(description = "濂楅绛夌骇", example = "PRO")
    private SubscriptionTier subscriptionTier;
    @Schema(description = "账号是否启用", example = "true")
    private Boolean enabled;
    @Schema(description = "每日请求配额", example = "1000")
    private Integer dailyRequestQuota;
    @Schema(description = "今日已请求次数", example = "12")
    private Integer todayRequestCount;
    @Schema(description = "今日剩余配额", example = "988")
    private Integer remainingQuota;
    @Schema(description = "默认模型", example = "qwen-max")
    private String preferredModel;
    @Schema(description = "鎸夋ā鍨嬪垎閰嶇殑閰嶉")
    private List<UserModelQuotaResponse> modelQuotas;
}
