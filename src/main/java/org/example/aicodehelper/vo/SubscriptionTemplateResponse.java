package org.example.aicodehelper.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.example.aicodehelper.domain.SubscriptionTier;

import java.util.List;

@Data
@Builder
@Schema(name = "SubscriptionTemplateResponse", description = "套餐默认策略模板")
public class SubscriptionTemplateResponse {

    @Schema(description = "套餐等级", example = "PRO")
    private SubscriptionTier subscriptionTier;

    @Schema(description = "默认每日总配额", example = "200")
    private Integer dailyRequestQuota;

    @Schema(description = "默认模型配额模板")
    private List<UserModelQuotaResponse> modelQuotas;
}
