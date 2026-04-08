package org.example.aicodehelper.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.example.aicodehelper.domain.ConversationMessage;

import java.util.List;

@Mapper
public interface ConversationMessageMapper {

    @Select("""
            select id, conversation_id, role, content, model_name, error_message, created_at
            from conversation_message
            where conversation_id = #{conversationId}
            order by id asc
            """)
    @Results(id = "conversationMessageResultMap", value = {
            @Result(column = "id", property = "id"),
            @Result(column = "conversation_id", property = "conversation.id"),
            @Result(column = "role", property = "role"),
            @Result(column = "content", property = "content"),
            @Result(column = "model_name", property = "modelName"),
            @Result(column = "error_message", property = "errorMessage"),
            @Result(column = "created_at", property = "createdAt")
    })
    List<ConversationMessage> findByConversationIdOrderByIdAsc(Long conversationId);

    @Select("select count(1) from conversation_message where conversation_id = #{conversationId}")
    long countByConversationId(Long conversationId);

    @Select("""
            select id, conversation_id, role, content, model_name, error_message, created_at
            from conversation_message
            where id = #{id} and conversation_id = #{conversationId}
            limit 1
            """)
    @ResultMap("conversationMessageResultMap")
    ConversationMessage findByIdAndConversationId(Long id, Long conversationId);

    @Insert("""
            insert into conversation_message (
                conversation_id, role, content, model_name, error_message, created_at
            ) values (
                #{conversation.id}, #{role}, #{content}, #{modelName}, #{errorMessage}, #{createdAt}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ConversationMessage message);

    @Delete("delete from conversation_message where id = #{id}")
    int deleteById(Long id);

    @Delete("delete from conversation_message where conversation_id = #{conversationId}")
    int deleteByConversationId(Long conversationId);

    @Delete("delete from conversation_message where conversation_id = #{conversationId} and id >= #{id}")
    int deleteByConversationIdAndIdGreaterThanEqual(Long conversationId, Long id);
}
