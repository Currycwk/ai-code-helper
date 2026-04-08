package org.example.aicodehelper.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.admin")
public class AppAdminProperties {
    private String bootstrapUsername = "admin";
    private String bootstrapPassword = "admin123456";
    private String bootstrapDisplayName = "System Admin";
}
