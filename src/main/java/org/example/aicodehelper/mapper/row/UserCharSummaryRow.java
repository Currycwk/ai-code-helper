package org.example.aicodehelper.mapper.row;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCharSummaryRow {
    private Long totalPromptChars;
    private Long totalCompletionChars;
}
