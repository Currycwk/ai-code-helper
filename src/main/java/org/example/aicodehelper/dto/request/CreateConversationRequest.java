package org.example.aicodehelper.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "CreateConversationRequest", description = "创建新会话请求")
public class CreateConversationRequest {

    @Schema(description = "可选自定义标题；为空时系统默认生成“新会话”。", example = "Java 学习计划")
    private String title;
}
