package org.example.aicodehelper.event;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "SseTokenUsage", description = "Token用量统计，包含输入、输出和总计的Token数量。")
public class SseTokenUsage {

    @Schema(description = "输入Token统计", example = "128")
    private Integer inputTokens;

    @Schema(description = "输出Token统计", example = "512")
    private Integer outputTokens;

    @Schema(description = "总Token统计", example = "640")
    private Integer totalTokens;
}
