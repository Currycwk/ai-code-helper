package org.example.aicodehelper.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.example.aicodehelper.domain.ApiCallRecord;
import org.example.aicodehelper.mapper.row.AuditLogRow;
import org.example.aicodehelper.mapper.row.DailyUsageRow;
import org.example.aicodehelper.mapper.row.ModelUsageRow;
import org.example.aicodehelper.mapper.row.RecentCallRow;
import org.example.aicodehelper.mapper.row.UserCharSummaryRow;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 调用审计 Mapper。
 * 负责访问 api_call_record 表，提供调用日志写入、按条件筛选、
 * 用户统计、趋势统计和管理员审计查询等数据访问能力。
 */
@Mapper
public interface ApiCallRecordMapper {

    @Insert("""
            insert into api_call_record (
                user_id, model_name, memory_id, request_length, response_length,
                request_preview, response_preview, latency_ms, success, error_message, created_at
            ) values (
                #{user.id}, #{modelName}, #{memoryId}, #{requestLength}, #{responseLength},
                #{requestPreview}, #{responsePreview}, #{latencyMs}, #{success}, #{errorMessage}, #{createdAt}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ApiCallRecord record);

    @Select("""
            select count(1)
            from api_call_record
            where user_id = #{userId} and created_at between #{start} and #{end}
            """)
    long countByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);

    @Select("""
            select count(1)
            from api_call_record
            where user_id = #{userId}
            """)
    long countByUserId(Long userId);

    @Select("""
            select count(1)
            from api_call_record
            where user_id = #{userId} and model_name = #{modelName} and created_at between #{start} and #{end}
            """)
    long countByUserIdAndModelNameAndCreatedAtBetween(Long userId, String modelName, LocalDateTime start, LocalDateTime end);

    @Select("""
            select count(1)
            from api_call_record
            where created_at between #{start} and #{end}
            """)
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Select("""
            select count(1)
            from api_call_record
            where success = true and created_at between #{start} and #{end}
            """)
    long countBySuccessTrueAndCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Select("""
            select r.model_name as model_name,
                   r.memory_id as memory_id,
                   cs.title as conversation_title,
                   r.request_length as request_length,
                   r.response_length as response_length,
                   r.latency_ms as latency_ms,
                   r.success,
                   r.error_message as error_message,
                   r.created_at as created_at
            from api_call_record r
            left join conversation_session cs on cs.id = r.memory_id
            where r.user_id = #{userId}
            order by r.created_at desc
            limit 20
            """)
    @Results(id = "recentCallRowResultMap", value = {
            @Result(column = "model_name", property = "modelName"),
            @Result(column = "memory_id", property = "memoryId"),
            @Result(column = "conversation_title", property = "conversationTitle"),
            @Result(column = "request_length", property = "requestLength"),
            @Result(column = "response_length", property = "responseLength"),
            @Result(column = "latency_ms", property = "latencyMs"),
            @Result(column = "success", property = "success"),
            @Result(column = "error_message", property = "errorMessage"),
            @Result(column = "created_at", property = "createdAt")
    })
    List<RecentCallRow> findTop20ByUserIdOrderByCreatedAtDesc(Long userId);

    @Select("""
            select coalesce(sum(request_length), 0) as total_prompt_chars,
                   coalesce(sum(response_length), 0) as total_completion_chars
            from api_call_record
            where user_id = #{userId}
            """)
    UserCharSummaryRow sumCharsByUserId(Long userId);

    @Select("""
            select date(created_at) as usage_date, count(*) as usage_count
            from api_call_record
            where created_at between #{start} and #{end}
            group by date(created_at)
            order by usage_date
            """)
    List<DailyUsageRow> countDailyUsage(LocalDateTime start, LocalDateTime end);

    @Select("""
            select model_name, count(*) as request_count
            from api_call_record
            where created_at between #{start} and #{end}
            group by model_name
            order by request_count desc
            """)
    List<ModelUsageRow> countModelUsage(LocalDateTime start, LocalDateTime end);

    @Select("""
            <script>
            select r.id,
                   r.user_id as user_id,
                   u.username,
                   r.model_name as model_name,
                   r.memory_id as memory_id,
                   cs.title as conversation_title,
                   r.request_length as request_length,
                   r.response_length as response_length,
                   r.request_preview as request_preview,
                   r.response_preview as response_preview,
                   r.latency_ms as latency_ms,
                   r.success,
                   r.error_message as error_message,
                   r.created_at as created_at
            from api_call_record r
            join user_account u on u.id = r.user_id
            left join conversation_session cs on cs.id = r.memory_id
            where 1 = 1
            <if test="userId != null">
                and r.user_id = #{userId}
            </if>
            <if test="modelName != null and modelName != ''">
                and r.model_name = #{modelName}
            </if>
            <if test="success != null">
                and r.success = #{success}
            </if>
            <if test="start != null">
                and r.created_at &gt;= #{start}
            </if>
            <if test="end != null">
                and r.created_at &lt;= #{end}
            </if>
            order by r.created_at desc
            limit #{limit}
            </script>
            """)
    @Results(id = "auditLogRowResultMap", value = {
            @Result(column = "id", property = "id"),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "username", property = "username"),
            @Result(column = "model_name", property = "modelName"),
            @Result(column = "memory_id", property = "memoryId"),
            @Result(column = "conversation_title", property = "conversationTitle"),
            @Result(column = "request_length", property = "requestLength"),
            @Result(column = "response_length", property = "responseLength"),
            @Result(column = "request_preview", property = "requestPreview"),
            @Result(column = "response_preview", property = "responsePreview"),
            @Result(column = "latency_ms", property = "latencyMs"),
            @Result(column = "success", property = "success"),
            @Result(column = "error_message", property = "errorMessage"),
            @Result(column = "created_at", property = "createdAt")
    })
    List<AuditLogRow> searchAuditLogs(Long userId,
                                      String modelName,
                                      Boolean success,
                                      LocalDateTime start,
                                      LocalDateTime end,
                                      int limit);
}
