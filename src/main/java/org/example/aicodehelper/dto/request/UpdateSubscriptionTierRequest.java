package org.example.aicodehelper.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.aicodehelper.domain.SubscriptionTier;

@Data
@Schema(name = "UpdateSubscriptionTierRequest", description = "更新套餐等级请求")
public class UpdateSubscriptionTierRequest {

    @NotNull
    @Schema(description = "套餐等级", example = "PRO")
    private SubscriptionTier subscriptionTier;
}
