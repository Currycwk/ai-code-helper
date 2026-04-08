package org.example.aicodehelper.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
@Schema(name = "UsageTrendPointResponse", description = "按天统计的调用趋势点")
public class UsageTrendPointResponse {
    @Schema(description = "日期", example = "2026-03-24")
    private LocalDate date;
    @Schema(description = "当天请求量", example = "26")
    private long requestCount;
}
