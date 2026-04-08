package org.example.aicodehelper.service;

import lombok.RequiredArgsConstructor;
import org.example.aicodehelper.domain.SubscriptionTier;
import org.example.aicodehelper.domain.UserAccount;
import org.example.aicodehelper.domain.UserModelQuota;
import org.example.aicodehelper.vo.SubscriptionTemplateResponse;
import org.example.aicodehelper.vo.UserModelQuotaResponse;
import org.example.aicodehelper.mapper.UserModelQuotaMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SubscriptionPolicyService {

    private final ModelCatalogService modelCatalogService;
    private final UserModelQuotaMapper userModelQuotaMapper;

    public List<SubscriptionTemplateResponse> listTemplates() {
        return List.of(
                buildTemplate(SubscriptionTier.FREE),
                buildTemplate(SubscriptionTier.PRO),
                buildTemplate(SubscriptionTier.ENTERPRISE)
        );
    }

    public Integer resolveDefaultDailyQuota(SubscriptionTier subscriptionTier) {
        return switch (subscriptionTier) {
            case FREE -> 20;
            case PRO -> 200;
            case ENTERPRISE -> 1000;
        };
    }

    public Map<String, Integer> resolveDefaultModelQuotas(SubscriptionTier subscriptionTier) {
        List<String> models = modelCatalogService.listAvailableModels().stream()
                .map(model -> model.getName())
                .toList();
        String defaultModel = modelCatalogService.getDefaultModel();
        Map<String, Integer> quotas = new LinkedHashMap<>();
        for (String modelName : models) {
            int quota = switch (subscriptionTier) {
                case FREE -> modelName.equals(defaultModel) ? 20 : 5;
                case PRO -> modelName.equals(defaultModel) ? 200 : 80;
                case ENTERPRISE -> 1000;
            };
            quotas.put(modelName, quota);
        }
        return quotas;
    }

    @Transactional
    public void applyTemplate(UserAccount user, SubscriptionTier subscriptionTier) {
        user.setSubscriptionTier(subscriptionTier);
        user.setDailyRequestQuota(resolveDefaultDailyQuota(subscriptionTier));

        Map<String, Integer> modelQuotas = resolveDefaultModelQuotas(subscriptionTier);
        List<UserModelQuota> existing = userModelQuotaMapper.findByUserIdOrderByModelNameAsc(user.getId());
        Map<String, UserModelQuota> existingMap = new LinkedHashMap<>();
        for (UserModelQuota item : existing) {
            existingMap.put(item.getModelName(), item);
        }

        for (Map.Entry<String, Integer> entry : modelQuotas.entrySet()) {
            UserModelQuota quota = existingMap.get(entry.getKey());
            if (quota == null) {
                quota = new UserModelQuota();
                quota.setUser(user);
                quota.setModelName(entry.getKey());
            }
            quota.setDailyQuota(entry.getValue());
            if (quota.getId() == null) {
                userModelQuotaMapper.insert(quota);
            } else {
                userModelQuotaMapper.update(quota);
            }
        }
    }

    private SubscriptionTemplateResponse buildTemplate(SubscriptionTier subscriptionTier) {
        return SubscriptionTemplateResponse.builder()
                .subscriptionTier(subscriptionTier)
                .dailyRequestQuota(resolveDefaultDailyQuota(subscriptionTier))
                .modelQuotas(resolveDefaultModelQuotas(subscriptionTier).entrySet().stream()
                        .map(entry -> UserModelQuotaResponse.builder()
                                .modelName(entry.getKey())
                                .dailyQuota(entry.getValue())
                                .build())
                        .toList())
                .build();
    }
}
