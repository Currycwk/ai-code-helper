package org.example.aicodehelper.event;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "SseChatEvent", description = "SSE 聊天事件，包含增量文本、错误信息和完成元数据等不同类型的事件内容。")
public class SseChatEvent {

    @Schema(description = "事件类型", example = "delta")
    private String type;

    @Schema(description = "增量文本片段，仅在 delta事件中存在。", example = "这是一个为期三个月的 Java 学习计划。")
    private String delta;

    @Schema(description = "错误信息，仅在 error事件中存在。", example = "每日配额已耗尽")
    private String error;

    @Schema(description = "完成元数据，仅在 done事件中存在。")
    private SseChatMeta meta;
}
