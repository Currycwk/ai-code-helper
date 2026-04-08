package org.example.aicodehelper.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.aicodehelper.domain.ConversationSession;

import java.util.List;

@Mapper
public interface ConversationSessionMapper {

    @Select("""
            select id, user_id, title, manual_title, last_message_preview, created_at, updated_at
            from conversation_session
            where id = #{id} and user_id = #{userId}
            limit 1
            """)
    @Results(id = "conversationSessionResultMap", value = {
            @Result(column = "id", property = "id"),
            @Result(column = "user_id", property = "user.id"),
            @Result(column = "title", property = "title"),
            @Result(column = "manual_title", property = "manualTitle"),
            @Result(column = "last_message_preview", property = "lastMessagePreview"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt")
    })
    ConversationSession findByIdAndUserId(Long id, Long userId);

    @Select("""
            select id, user_id, title, manual_title, last_message_preview, created_at, updated_at
            from conversation_session
            where user_id = #{userId}
            order by updated_at desc
            """)
    @ResultMap("conversationSessionResultMap")
    List<ConversationSession> findByUserIdOrderByUpdatedAtDesc(Long userId);

    @Insert("""
            insert into conversation_session (
                user_id, title, manual_title, last_message_preview, created_at, updated_at
            ) values (
                #{user.id}, #{title}, #{manualTitle}, #{lastMessagePreview}, #{createdAt}, #{updatedAt}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ConversationSession session);

    @Update("""
            update conversation_session
            set title = #{title},
                manual_title = #{manualTitle},
                last_message_preview = #{lastMessagePreview},
                updated_at = #{updatedAt}
            where id = #{id}
            """)
    int update(ConversationSession session);

    @Delete("delete from conversation_session where id = #{id}")
    int deleteById(Long id);
}
