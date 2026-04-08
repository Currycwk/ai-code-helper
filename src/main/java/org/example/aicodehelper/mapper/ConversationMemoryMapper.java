package org.example.aicodehelper.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.aicodehelper.domain.ConversationMemory;

@Mapper
public interface ConversationMemoryMapper {

    @Select("""
            select conversation_id, messages_json, updated_at
            from conversation_memory
            where conversation_id = #{conversationId}
            """)
    @Results({
            @Result(column = "conversation_id", property = "conversationId"),
            @Result(column = "messages_json", property = "messagesJson"),
            @Result(column = "updated_at", property = "updatedAt")
    })
    ConversationMemory findByConversationId(Long conversationId);

    @Insert("""
            insert into conversation_memory (conversation_id, messages_json, updated_at)
            values (#{conversationId}, #{messagesJson}, #{updatedAt})
            """)
    int insert(ConversationMemory memory);

    @Update("""
            update conversation_memory
            set messages_json = #{messagesJson},
                updated_at = #{updatedAt}
            where conversation_id = #{conversationId}
            """)
    int update(ConversationMemory memory);

    @Delete("delete from conversation_memory where conversation_id = #{conversationId}")
    int deleteByConversationId(Long conversationId);
}
