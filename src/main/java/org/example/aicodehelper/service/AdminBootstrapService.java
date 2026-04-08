package org.example.aicodehelper.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.example.aicodehelper.config.AppAdminProperties;
import org.example.aicodehelper.domain.UserAccount;
import org.example.aicodehelper.domain.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminBootstrapService {

    private final AppAdminProperties properties;
    private final UserAccountService userAccountService;
    private final PasswordEncoder passwordEncoder;
    private final ModelCatalogService modelCatalogService;

    @PostConstruct
    public void ensureAdminExists() {
        if (userAccountService.existsByUsername(properties.getBootstrapUsername())) {
            return;
        }
        UserAccount admin = new UserAccount();
        admin.setUsername(properties.getBootstrapUsername());
        admin.setDisplayName(properties.getBootstrapDisplayName());
        admin.setPasswordHash(passwordEncoder.encode(properties.getBootstrapPassword()));
        admin.setRole(UserRole.ADMIN);
        admin.setDailyRequestQuota(1000);
        admin.setPreferredModel(modelCatalogService.getDefaultModel());
        userAccountService.createUser(admin);
    }
}
