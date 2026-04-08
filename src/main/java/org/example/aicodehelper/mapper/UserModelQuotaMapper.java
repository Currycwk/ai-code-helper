package org.example.aicodehelper.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.aicodehelper.domain.UserModelQuota;

import java.util.List;

/**
 * 用户模型配额 Mapper。
 * 负责 user_model_quota 表的查询与维护，
 * 用于按模型粒度限制用户每日可用调用次数。
 */
@Mapper
public interface UserModelQuotaMapper {

    @Select("""
            select id, user_id, model_name, daily_quota
            from user_model_quota
            where user_id = #{userId}
            order by model_name asc
            """)
    @Results(id = "userModelQuotaResultMap", value = {
            @Result(column = "id", property = "id"),
            @Result(column = "user_id", property = "user.id"),
            @Result(column = "model_name", property = "modelName"),
            @Result(column = "daily_quota", property = "dailyQuota")
    })
    List<UserModelQuota> findByUserIdOrderByModelNameAsc(Long userId);

    @Select("""
            select id, user_id, model_name, daily_quota
            from user_model_quota
            where user_id = #{userId} and model_name = #{modelName}
            limit 1
            """)
    @org.apache.ibatis.annotations.ResultMap("userModelQuotaResultMap")
    UserModelQuota findByUserIdAndModelName(Long userId, String modelName);

    @Insert("""
            insert into user_model_quota (user_id, model_name, daily_quota)
            values (#{user.id}, #{modelName}, #{dailyQuota})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserModelQuota quota);

    @Update("""
            update user_model_quota
            set daily_quota = #{dailyQuota}
            where id = #{id}
            """)
    int update(UserModelQuota quota);

    @Delete("delete from user_model_quota where user_id = #{userId}")
    int deleteByUserId(Long userId);
}
