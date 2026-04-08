package org.example.aicodehelper.event;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "SseChatMeta", description = "SSE 聊天完成事件的元数据，包含模型名称、耗时和 Token 用量统计等信息。")
public class SseChatMeta {

    @Schema(description = "模型名称", example = "qwen-max")
    private String model;

    @Schema(description = "耗时（毫秒）", example = "1250")
    private Long elapsedMs;

    @Schema(description = "Token用量统计")
    private SseTokenUsage tokens;
}
