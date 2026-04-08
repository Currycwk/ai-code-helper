package org.example.aicodehelper.guardrail;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailResult;

import java.util.Set;

/**
 * 安全输入防护（Safe Input Guardrail）是指在与AI模型交互时，采取措施确保输入内容的安全性和合规性。这些措施可以包括：
 * 1. 输入验证：对用户输入进行验证，确保其符合预期的格式和内容要求，防止恶意输入。
 * 2. 内容过滤：使用关键词过滤或正则表达式等技术，过滤掉敏感词汇、恶意代码或不适当的内容。
 * 3. 权限控制：根据用户的权限级别，限制其输入的范围和类型，防止越权操作。
 * 4. 日志记录：记录用户输入的内容和相关操作，以便后续审计和分析，及时发现和应对潜在的安全威胁。
 * 5. 模型输出监控：监控AI模型的输出，确保其不包含敏感信息或不适当的内容，及时进行干预和修正。
 * 通过实施安全输入防护措施，可以有效降低与AI模型交互过程中可能出现的安全风险，保护用户和系统的安全与隐私。
 */
public class SafeInputGuardrail implements InputGuardrail {
    private static final Set<String> sensitiveWords=Set.of("kill","evil");

    @Override
    public InputGuardrailResult validate(UserMessage userMessage) {
        //转换为小写以进行不区分大小写的检查
        String inputText=userMessage.singleText().toLowerCase();
        //使用正则表达式分割输入文本为单词
        String[] words = inputText.split("\\W+");
        //遍历所有单词
        for(String word: words){
            if(sensitiveWords.contains(word)){
                return fatal("输入包含敏感词汇，已被拒绝："+word);
            }
        }
        return success();
    }
}
