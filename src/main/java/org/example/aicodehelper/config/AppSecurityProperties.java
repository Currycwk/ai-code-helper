package org.example.aicodehelper.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.security")
public class AppSecurityProperties {
    private String jwtSecret;
    private long expirationHours = 72;
}
