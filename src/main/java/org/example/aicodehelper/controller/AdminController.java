package org.example.aicodehelper.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.aicodehelper.dto.request.UpdateModelQuotaRequest;
import org.example.aicodehelper.dto.request.UpdateQuotaRequest;
import org.example.aicodehelper.dto.request.UpdateRoleRequest;
import org.example.aicodehelper.dto.request.UpdateSubscriptionTierRequest;
import org.example.aicodehelper.dto.request.UpdateUserStatusRequest;
import org.example.aicodehelper.vo.AdminDashboardResponse;
import org.example.aicodehelper.vo.AuditLogResponse;
import org.example.aicodehelper.vo.SubscriptionTemplateResponse;
import org.example.aicodehelper.vo.UsageTrendPointResponse;
import org.example.aicodehelper.vo.UserModelQuotaResponse;
import org.example.aicodehelper.vo.UserProfileResponse;
import org.example.aicodehelper.service.AdminService;
import org.example.aicodehelper.service.UserAccountService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.time.LocalDateTime;

/**
 * 管理员后台控制器。
 * 统一暴露后台运营相关接口，包括用户管理、套餐与配额管理、
 * 调用趋势统计和审计日志查询，供管理员界面使用。
 */
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "管理员后台接口，包括仪表盘、用户管理、模型配额和审计日志")
public class AdminController {

    private final AdminService adminService;
    private final UserAccountService userAccountService;

    @GetMapping("/dashboard")
    @Operation(summary = "管理员仪表盘", description = "返回总用户数、今日请求量、今日成功请求量及今日模型使用排行。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "403", description = "无管理员权限", content = @Content)
    })
    public AdminDashboardResponse dashboard() {
        return adminService.dashboard();
    }

    @GetMapping("/users")
    @Operation(summary = "查询所有用户", description = "返回所有用户的基础资料、套餐、启用状态、配额、模型配额与默认模型。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "403", description = "无管理员权限", content = @Content)
    })
    public List<UserProfileResponse> users(@Parameter(description = "可选用户 ID 过滤", example = "2")
                                           @RequestParam(required = false) Long userId) {
        return adminService.listUsers(userId);
    }

    @PatchMapping("/users/{userId}/quota")
    @Operation(summary = "修改用户每日总配额", description = "按用户 ID 更新每日允许的总调用次数。")
    public UserProfileResponse updateQuota(@Parameter(description = "用户 ID", example = "2") @PathVariable long userId,
                                           @Valid @RequestBody UpdateQuotaRequest request) {
        return userAccountService.updateQuota(userId, request.getDailyRequestQuota());
    }

    @PatchMapping("/users/{userId}/status")
    @Operation(summary = "启用或停用用户", description = "按用户 ID 更新用户启用状态。")
    public UserProfileResponse updateStatus(@Parameter(description = "用户 ID", example = "2") @PathVariable long userId,
                                            @Valid @RequestBody UpdateUserStatusRequest request) {
        return adminService.updateEnabled(userId, request.getEnabled());
    }

    @PatchMapping("/users/{userId}/role")
    @Operation(summary = "修改用户角色", description = "按用户 ID 将角色修改为 USER 或 ADMIN。")
    public UserProfileResponse updateRole(@Parameter(description = "用户 ID", example = "2") @PathVariable long userId,
                                          @Valid @RequestBody UpdateRoleRequest request) {
        return adminService.updateRole(userId, request.getRole());
    }

    @PatchMapping("/users/{userId}/subscription-tier")
    @Operation(summary = "修改套餐等级", description = "按用户 ID 修改套餐等级为 FREE、PRO 或 ENTERPRISE。")
    public UserProfileResponse updateSubscriptionTier(@Parameter(description = "用户 ID", example = "2") @PathVariable long userId,
                                                      @Valid @RequestBody UpdateSubscriptionTierRequest request) {
        return adminService.updateSubscriptionTier(userId, request.getSubscriptionTier());
    }

    @GetMapping("/users/{userId}/model-quotas")
    @Operation(summary = "查询用户模型配额", description = "返回指定用户按模型维度配置的每日配额。")
    public List<UserModelQuotaResponse> getModelQuotas(@Parameter(description = "用户 ID", example = "2") @PathVariable long userId) {
        return adminService.getModelQuotas(userId);
    }

    @PatchMapping("/users/{userId}/model-quotas")
    @Operation(summary = "修改用户模型配额", description = "按用户 ID 和模型名更新指定模型的每日配额，0 表示禁用该模型。")
    public List<UserModelQuotaResponse> updateModelQuota(@Parameter(description = "用户 ID", example = "2") @PathVariable long userId,
                                                         @Valid @RequestBody UpdateModelQuotaRequest request) {
        return adminService.updateModelQuota(userId, request.getModelName(), request.getDailyQuota());
    }

    @GetMapping("/usage/trend")
    @Operation(summary = "查询调用趋势", description = "按天统计最近 N 天请求量，默认 7 天。")
    public List<UsageTrendPointResponse> trend(@Parameter(description = "查询最近多少天的趋势", example = "7") @RequestParam(defaultValue = "7") int days) {
        return adminService.trend(Math.max(days, 1));
    }

    @GetMapping("/audit-logs")
    @Operation(summary = "查询调用审计日志", description = "返回最近 100 条调用记录，可按用户过滤。")
    public List<AuditLogResponse> auditLogs(@Parameter(description = "可选用户 ID 过滤", example = "2") @RequestParam(required = false) Long userId,
                                            @Parameter(description = "可选模型过滤", example = "qwen-max") @RequestParam(required = false) String modelName,
                                            @Parameter(description = "可选成功状态过滤", example = "true") @RequestParam(required = false) Boolean success,
                                            @Parameter(description = "开始时间", example = "2026-03-25 00:00:00") @RequestParam(required = false) LocalDateTime start,
                                            @Parameter(description = "结束时间", example = "2026-03-25 23:59:59") @RequestParam(required = false) LocalDateTime end) {
        return adminService.auditLogs(userId, modelName, success, start, end);
    }

    @GetMapping("/subscription-templates")
    @Operation(summary = "查询套餐默认策略模板", description = "返回不同套餐等级对应的默认总配额和模型配额模板。")
    public List<SubscriptionTemplateResponse> subscriptionTemplates() {
        return adminService.subscriptionTemplates();
    }
}
