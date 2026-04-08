package org.example.aicodehelper.service;

import lombok.RequiredArgsConstructor;
import org.example.aicodehelper.domain.UserAccount;
import org.example.aicodehelper.domain.SubscriptionTier;
import org.example.aicodehelper.domain.UserRole;
import org.example.aicodehelper.vo.AdminDashboardResponse;
import org.example.aicodehelper.vo.AuditLogResponse;
import org.example.aicodehelper.vo.SubscriptionTemplateResponse;
import org.example.aicodehelper.vo.UserModelQuotaResponse;
import org.example.aicodehelper.vo.UsageTrendPointResponse;
import org.example.aicodehelper.vo.UserProfileResponse;
import org.example.aicodehelper.exception.BadRequestException;
import org.example.aicodehelper.mapper.ApiCallRecordMapper;
import org.example.aicodehelper.mapper.row.AuditLogRow;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private static final DateTimeFormatter AUDIT_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ApiCallRecordMapper apiCallRecordMapper;
    private final UserAccountService userAccountService;
    private final UsageService usageService;
    private final SubscriptionPolicyService subscriptionPolicyService;

    public AdminDashboardResponse dashboard() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        List<AdminDashboardResponse.ModelUsageItem> modelUsage = apiCallRecordMapper.countModelUsage(start, end).stream()
                .map(row -> AdminDashboardResponse.ModelUsageItem.builder()
                        .modelName(row.getModelName())
                        .requestCount(row.getRequestCount())
                        .build())
                .toList();
        return AdminDashboardResponse.builder()
                .totalUsers(userAccountService.countUsers())
                .todayRequests(apiCallRecordMapper.countByCreatedAtBetween(start, end))
                .todaySuccessRequests(apiCallRecordMapper.countBySuccessTrueAndCreatedAtBetween(start, end))
                .modelUsage(modelUsage)
                .build();
    }

    public List<UserProfileResponse> listUsers(Long userId) {
        return userAccountService.listProfiles(userId);
    }

    @Transactional
    public UserProfileResponse updateEnabled(long userId, boolean enabled) {
        if (userId == 1L && !enabled) {
            throw new BadRequestException("Cannot disable bootstrap admin");
        }
        return userAccountService.updateEnabled(userId, enabled);
    }

    @Transactional
    public UserProfileResponse updateRole(long userId, UserRole role) {
        UserAccount user = userAccountService.getRequiredById(userId);
        if (user.getId() != null && user.getId() == 1L && role != UserRole.ADMIN) {
            throw new BadRequestException("Cannot downgrade bootstrap admin");
        }
        user.setRole(role);
        userAccountService.saveUser(user);
        return userAccountService.toProfile(user);
    }

    @Transactional
    public UserProfileResponse updateSubscriptionTier(long userId, SubscriptionTier subscriptionTier) {
        UserAccount user = userAccountService.getRequiredById(userId);
        subscriptionPolicyService.applyTemplate(user, subscriptionTier);
        return userAccountService.toProfile(user);
    }

    @Transactional
    public List<UserModelQuotaResponse> updateModelQuota(long userId, String modelName, int dailyQuota) {
        return userAccountService.updateModelQuota(userId, modelName, dailyQuota);
    }

    public List<UserModelQuotaResponse> getModelQuotas(long userId) {
        return userAccountService.getModelQuotas(userId);
    }

    public List<UsageTrendPointResponse> trend(int days) {
        return usageService.buildTrend(days);
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> auditLogs(Long userId,
                                            String modelName,
                                            Boolean success,
                                            LocalDateTime start,
                                            LocalDateTime end) {
        List<AuditLogRow> records = apiCallRecordMapper.searchAuditLogs(
                userId,
                modelName == null || modelName.isBlank() ? null : modelName,
                success,
                start,
                end,
                100
        );
        return records.stream()
                .map(record -> AuditLogResponse.builder()
                        .id(record.getId())
                        .userId(record.getUserId())
                        .username(record.getUsername())
                        .modelName(record.getModelName())
                        .memoryId(record.getMemoryId())
                        .conversationTitle(record.getConversationTitle())
                        .requestPreview(record.getRequestPreview())
                        .responsePreview(record.getResponsePreview())
                        .requestLength(record.getRequestLength())
                        .responseLength(record.getResponseLength())
                        .latencyMs(record.getLatencyMs())
                        .success(record.getSuccess())
                        .errorMessage(record.getErrorMessage())
                        .createdAt(record.getCreatedAt() == null ? null : record.getCreatedAt().format(AUDIT_TIME_FORMATTER))
                        .build())
                .toList();
    }

    public List<SubscriptionTemplateResponse> subscriptionTemplates() {
        return subscriptionPolicyService.listTemplates();
    }
}
