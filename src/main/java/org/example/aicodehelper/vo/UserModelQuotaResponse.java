package org.example.aicodehelper.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(name = "UserModelQuotaResponse", description = "按模型分配的用户配额")
public class UserModelQuotaResponse {

    @Schema(description = "模型名称", example = "qwen-max")
    private String modelName;

    @Schema(description = "模型每日配额", example = "50")
    private Integer dailyQuota;
}
