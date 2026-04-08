package org.example.aicodehelper.mapper.row;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModelUsageRow {
    private String modelName;
    private Long requestCount;
}
