package org.example.aicodehelper;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

/**
 * 项目启动入口。
 * 负责启动 Spring Boot 应用、扫描 MyBatis Mapper，并在应用就绪后输出接口文档地址，
 * 方便本地开发时快速进入 Knife4j 和 OpenAPI 文档页面。
 */
@SpringBootApplication
@Slf4j
@MapperScan("org.example.aicodehelper.mapper")
public class AiCodeHelperApplication {

    private final Environment environment;

    public AiCodeHelperApplication(Environment environment) {
        this.environment = environment;
    }

    public static void main(String[] args) {
        SpringApplication.run(AiCodeHelperApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logSwaggerUrl() {
        String port = environment.getProperty("server.port", "8080");
        String contextPath = environment.getProperty("server.servlet.context-path", "");
        log.info("Knife4j UI: http://localhost:{}{}/doc.html", port, contextPath);
        log.info("OpenAPI JSON: http://localhost:{}{}/v3/api-docs", port, contextPath);
    }

}
