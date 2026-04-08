package org.example.aicodehelper;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

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
