package org.example.aicodehelper.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(name = "UpdateQuotaRequest", description = "修改用户每日配额请求")
public class UpdateQuotaRequest {
    @Schema(description = "每日允许调用次数", example = "50")
    @NotNull
    @Min(1)
    @Max(10000)
    private Integer dailyRequestQuota;
}
