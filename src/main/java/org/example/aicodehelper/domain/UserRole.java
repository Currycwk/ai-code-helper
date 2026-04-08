package org.example.aicodehelper.domain;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 用户角色枚举。
 * 用于区分普通用户和管理员，
 * 决定用户可以访问哪些接口和后台功能。
 */
@Schema(description = "用户角色")
public enum UserRole {
    @Schema(description = "普通用户")
    USER,
    @Schema(description = "管理员")
    ADMIN
}
