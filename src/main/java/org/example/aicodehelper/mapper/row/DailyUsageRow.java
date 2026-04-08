package org.example.aicodehelper.mapper.row;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class DailyUsageRow {
    private LocalDate usageDate;
    private Long usageCount;
}
