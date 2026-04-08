package org.example.aicodehelper;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
@Slf4j
public class AiCodeHelper {

    private static final Pattern PROMPT_HEADER = Pattern.compile("^\\s*\\[prompt:(.+?)]\\s*$");

    @Resource
    private ChatModel qwenChatModel;

    @Value("classpath:system-prompt.txt")
    private org.springframework.core.io.Resource systemPromptResource;

    @Value("${app.prompts.default-key:code-helper}")
    private String systemPromptKey;

    private String systemPrompt;

    @PostConstruct
    void initPrompt() {
        systemPrompt = loadPrompt(systemPromptResource, systemPromptKey);
    }

    public String chat(String message) {
        SystemMessage systemMessage = SystemMessage.from(systemPrompt);
        UserMessage userMessage = UserMessage.from(message);
        ChatResponse chatResponse = qwenChatModel.chat(systemMessage, userMessage);
        AiMessage aiMessage = chatResponse.aiMessage();
        log.info("AI 输出：" + aiMessage.toString());
        return aiMessage.text();
    }


    public String chatWithMessage(UserMessage userMessage) {
        ChatResponse chatResponse = qwenChatModel.chat(userMessage);
        AiMessage aiMessage = chatResponse.aiMessage();
        log.info("AI 输出：" + aiMessage.toString());
        return aiMessage.text();
    }



    private static String loadPrompt(org.springframework.core.io.Resource resource, String key) {
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
