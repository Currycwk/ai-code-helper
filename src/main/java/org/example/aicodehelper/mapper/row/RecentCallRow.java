package org.example.aicodehelper.mapper.row;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RecentCallRow {
    private String modelName;
    private Long memoryId;
    private String conversationTitle;
    private Integer requestLength;
    private Integer responseLength;
    private Long latencyMs;
    private Boolean success;
    private String errorMessage;
    private LocalDateTime createdAt;
}
