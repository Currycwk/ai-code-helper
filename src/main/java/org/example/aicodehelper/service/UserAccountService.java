package org.example.aicodehelper.service;

import lombok.RequiredArgsConstructor;
import org.example.aicodehelper.domain.UserAccount;
import org.example.aicodehelper.domain.UserModelQuota;
import org.example.aicodehelper.domain.SubscriptionTier;
import org.example.aicodehelper.vo.UserModelQuotaResponse;
import org.example.aicodehelper.vo.UserProfileResponse;
import org.example.aicodehelper.exception.NotFoundException;
import org.example.aicodehelper.mapper.UserAccountMapper;
import org.example.aicodehelper.mapper.UserModelQuotaMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户账户服务。
 * 负责用户查询、资料转换、启停状态更新、套餐与配额更新，以及默认模型和模型配额管理，
 * 是用户体系相关业务逻辑的核心封装层。
 */
@Service
@RequiredArgsConstructor
public class UserAccountService {

    private final UserAccountMapper userAccountMapper;
    private final UsageService usageService;
    private final ModelCatalogService modelCatalogService;
    private final UserModelQuotaMapper userModelQuotaMapper;

    public UserAccount getRequiredById(Long id) {
        UserAccount user = userAccountMapper.findById(id);
        if (user == null) {
            throw new NotFoundException("User not found");
        }
        return user;
    }

    public UserAccount getRequiredByUsername(String username) {
        UserAccount user = userAccountMapper.findByUsername(username);
        if (user == null) {
            throw new NotFoundException("User not found");
        }
        return user;
    }

    public List<UserProfileResponse> listProfiles() {
        return userAccountMapper.findAllByOrderByIdAsc().stream()
                .map(this::toProfile)
                .toList();
    }

    public List<UserProfileResponse> listProfiles(Long userId) {
        if (userId == null) {
            return listProfiles();
        }
        return List.of(getRequiredById(userId)).stream().map(this::toProfile).toList();
    }

    @Transactional
    public UserProfileResponse updateQuota(Long userId, int dailyQuota) {
        UserAccount user = getRequiredById(userId);
        user.setDailyRequestQuota(dailyQuota);
        touchAndUpdate(user);
        return toProfile(user);
    }

    @Transactional
    public UserProfileResponse updateEnabled(Long userId, boolean enabled) {
        UserAccount user = getRequiredById(userId);
        user.setEnabled(enabled);
        touchAndUpdate(user);
        return toProfile(user);
    }

    @Transactional
    public UserProfileResponse updateSubscriptionTier(Long userId, SubscriptionTier subscriptionTier) {
        UserAccount user = getRequiredById(userId);
        user.setSubscriptionTier(subscriptionTier);
        touchAndUpdate(user);
        return toProfile(user);
    }

    @Transactional
    public List<UserModelQuotaResponse> updateModelQuota(Long userId, String modelName, int dailyQuota) {
        UserAccount user = getRequiredById(userId);
        String resolvedModelName = modelCatalogService.resolvePreferredModel(modelName);
        UserModelQuota quota = userModelQuotaMapper.findByUserIdAndModelName(userId, resolvedModelName);
        if (quota == null) {
            quota = new UserModelQuota();
            quota.setUser(user);
            quota.setModelName(resolvedModelName);
        }
        quota.setDailyQuota(dailyQuota);
        if (quota.getId() == null) {
            userModelQuotaMapper.insert(quota);
        } else {
            userModelQuotaMapper.update(quota);
        }
        return getModelQuotas(user);
    }

    @Transactional
    public UserProfileResponse updatePreferredModel(UserAccount user, String modelName) {
        user.setPreferredModel(modelCatalogService.resolvePreferredModel(modelName));
        touchAndUpdate(user);
        return toProfile(user);
    }

    public Integer getModelQuota(UserAccount user, String modelName) {
        UserModelQuota quota = userModelQuotaMapper.findByUserIdAndModelName(user.getId(), modelName);
        return quota == null ? null : quota.getDailyQuota();
    }

    public List<UserModelQuotaResponse> getModelQuotas(UserAccount user) {
        return userModelQuotaMapper.findByUserIdOrderByModelNameAsc(user.getId()).stream()
                .map(item -> UserModelQuotaResponse.builder()
                        .modelName(item.getModelName())
                        .dailyQuota(item.getDailyQuota())
                        .build())
                .toList();
    }

    public List<UserModelQuotaResponse> getModelQuotas(Long userId) {
        return getModelQuotas(getRequiredById(userId));
    }

    public UserProfileResponse toProfile(UserAccount user) {
        int today = usageService.countTodayRequests(user);
        int quota = user.getDailyRequestQuota() == null ? 0 : user.getDailyRequestQuota();
        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .role(user.getRole())
                .subscriptionTier(user.getSubscriptionTier())
                .enabled(user.getEnabled())
                .dailyRequestQuota(quota)
                .todayRequestCount(today)
                .remainingQuota(Math.max(quota - today, 0))
                .preferredModel(modelCatalogService.resolvePreferredModel(user.getPreferredModel()))
                .modelQuotas(getModelQuotas(user))
                .build();
    }

    @Transactional
    public void createUser(UserAccount user) {
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        userAccountMapper.insert(user);
    }

    @Transactional
    public void saveUser(UserAccount user) {
        if (user.getId() == null) {
            createUser(user);
            return;
        }
        touchAndUpdate(user);
    }

    public boolean existsByUsername(String username) {
        return userAccountMapper.countByUsername(username) > 0;
    }

    public long countUsers() {
        return userAccountMapper.countAll();
    }

    private void touchAndUpdate(UserAccount user) {
        user.setUpdatedAt(LocalDateTime.now());
        userAccountMapper.update(user);
    }
}
