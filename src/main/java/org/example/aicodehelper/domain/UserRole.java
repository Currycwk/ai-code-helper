package org.example.aicodehelper.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "用户角色")
public enum UserRole {
    @Schema(description = "普通用户")
    USER,
    @Schema(description = "管理员")
    ADMIN
}
