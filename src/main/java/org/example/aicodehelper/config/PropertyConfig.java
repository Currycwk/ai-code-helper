package org.example.aicodehelper.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        AppSecurityProperties.class,
        AppAdminProperties.class,
        AppModelCatalogProperties.class
})
public class PropertyConfig {
}
