package org.example.aicodehelper.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PromptService {

    private static final Pattern PROMPT_HEADER = Pattern.compile("^\\s*\\[prompt:(.+?)]\\s*$");

    @Value("classpath:system-prompt.txt")
    private Resource systemPromptResource;

    @Value("${app.prompts.default-key:code-helper}")
    private String systemPromptKey;

    private String systemPrompt;

    @PostConstruct
    public void init() {
        systemPrompt = loadPrompt(systemPromptResource, systemPromptKey);
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    private static String loadPrompt(Resource resource, String key) {
        Map<String, StringBuilder> prompts = new LinkedHashMap<>();
        String currentKey = null;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = PROMPT_HEADER.matcher(line);
                if (matcher.matches()) {
                    currentKey = matcher.group(1).trim();
                    prompts.putIfAbsent(currentKey, new StringBuilder());
                    continue;
                }
                if (currentKey != null) {
                    prompts.get(currentKey).append(line).append('\n');
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load system prompts", ex);
        }
        StringBuilder prompt = prompts.get(key);
        if (prompt == null) {
            throw new IllegalStateException("Prompt key not found: " + key);
        }
        return prompt.toString().trim();
    }
}
