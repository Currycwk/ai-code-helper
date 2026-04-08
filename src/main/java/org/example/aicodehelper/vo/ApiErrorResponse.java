package org.example.aicodehelper.vo;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ApiErrorResponse {
    String timestamp;
    int status;
    String error;
    String message;
    String path;
}
