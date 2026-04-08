package org.example.aicodehelper.service;

import lombok.RequiredArgsConstructor;
import org.example.aicodehelper.domain.UserAccount;
import org.example.aicodehelper.exception.ForbiddenException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class ChatFacadeService {

    private final ModelBackedAiServiceFactory modelBackedAiServiceFactory;
    private final UsageService usageService;
    private final ModelCatalogService modelCatalogService;
    private final UserAccountService userAccountService;

    public String resolveModel(UserAccount user) {
        return modelCatalogService.resolvePreferredModel(user.getPreferredModel());
    }

    public void ensureQuota(UserAccount user, String modelName) {
        if (!Boolean.TRUE.equals(user.getEnabled())) {
            throw new ForbiddenException("User account is disabled");
        }
        int todayCount = usageService.countTodayRequests(user);
        int quota = user.getDailyRequestQuota() == null ? 0 : user.getDailyRequestQuota();
        if (todayCount >= quota) {
            throw new ForbiddenException("Daily quota exhausted");
        }
        Integer modelQuota = userAccountService.getModelQuota(user, modelName);
        if (modelQuota != null) {
            int modelCount = usageService.countTodayRequestsByModel(user, modelName);
            if (modelCount >= modelQuota) {
                throw new ForbiddenException("Model quota exhausted for " + modelName);
            }
        }
    }

    public Flux<String> chatStream(UserAccount user, Long conversationId, String message) {
        String modelName = resolveModel(user);
        return modelBackedAiServiceFactory.getService(modelName).chatStream(conversationId, message);
    }
}
