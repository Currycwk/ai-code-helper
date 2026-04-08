package org.example.aicodehelper.service;

import lombok.RequiredArgsConstructor;
import org.example.aicodehelper.domain.ApiCallRecord;
import org.example.aicodehelper.domain.UserAccount;
import org.example.aicodehelper.mapper.ApiCallRecordMapper;
import org.example.aicodehelper.mapper.row.UserCharSummaryRow;
import org.example.aicodehelper.vo.UsageTrendPointResponse;
import org.example.aicodehelper.vo.UserUsageSummaryResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsageService {

    private static final DateTimeFormatter RECENT_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ApiCallRecordMapper apiCallRecordMapper;

    public int countTodayRequests(UserAccount user) {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        return (int) apiCallRecordMapper.countByUserIdAndCreatedAtBetween(user.getId(), start, end);
    }

    public int countTodayRequestsByModel(UserAccount user, String modelName) {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        return (int) apiCallRecordMapper.countByUserIdAndModelNameAndCreatedAtBetween(user.getId(), modelName, start, end);
    }

    public void record(UserAccount user,
                       String modelName,
                       Long memoryId,
                       String requestText,
                       String responseText,
                       long latencyMs,
                       boolean success,
                       String errorMessage) {
        ApiCallRecord record = new ApiCallRecord();
        record.setUser(user);
        record.setModelName(modelName);
        record.setMemoryId(memoryId);
        record.setRequestLength(requestText == null ? 0 : requestText.length());
        record.setResponseLength(responseText == null ? 0 : responseText.length());
        record.setRequestPreview(preview(requestText));
        record.setResponsePreview(preview(responseText));
        record.setLatencyMs(latencyMs);
        record.setSuccess(success);
        record.setErrorMessage(errorMessage);
        record.setCreatedAt(LocalDateTime.now());
        apiCallRecordMapper.insert(record);
    }

    public UserUsageSummaryResponse buildUserSummary(UserAccount user) {
        UserCharSummaryRow sums = apiCallRecordMapper.sumCharsByUserId(user.getId());
        List<UserUsageSummaryResponse.RecentCallItem> recentCalls = apiCallRecordMapper.findTop20ByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(record -> UserUsageSummaryResponse.RecentCallItem.builder()
                        .modelName(record.getModelName())
                        .memoryId(record.getMemoryId() == null ? 0L : record.getMemoryId())
                        .conversationTitle(record.getConversationTitle())
                        .requestLength(record.getRequestLength())
                        .responseLength(record.getResponseLength())
                        .latencyMs(record.getLatencyMs())
                        .success(Boolean.TRUE.equals(record.getSuccess()))
                        .errorMessage(record.getErrorMessage())
                        .createdAt(record.getCreatedAt() == null ? null : record.getCreatedAt().format(RECENT_TIME_FORMATTER))
                        .build())
                .toList();
        return UserUsageSummaryResponse.builder()
                .totalRequests(apiCallRecordMapper.countByUserId(user.getId()))
                .totalPromptChars(sums == null || sums.getTotalPromptChars() == null ? 0L : sums.getTotalPromptChars())
                .totalCompletionChars(sums == null || sums.getTotalCompletionChars() == null ? 0L : sums.getTotalCompletionChars())
                .recentCalls(recentCalls)
                .build();
    }

    public List<UsageTrendPointResponse> buildTrend(int days) {
        LocalDate startDate = LocalDate.now().minusDays(days - 1L);
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = LocalDate.now().plusDays(1L).atStartOfDay();
        return apiCallRecordMapper.countDailyUsage(start, end).stream()
                .map(row -> UsageTrendPointResponse.builder()
                        .date(row.getUsageDate())
                        .requestCount(row.getUsageCount())
                        .build())
                .toList();
    }

    private String preview(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        String normalized = text.replaceAll("\\s+", " ").trim();
        return normalized.length() > 120 ? normalized.substring(0, 120) + "..." : normalized;
    }
}
