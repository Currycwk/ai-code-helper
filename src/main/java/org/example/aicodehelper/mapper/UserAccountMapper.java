package org.example.aicodehelper.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.aicodehelper.domain.UserAccount;

import java.util.List;

/**
 * 用户账户 Mapper。
 * 负责 user_account 表的基础读写，
 * 是登录鉴权、用户管理、套餐与配额更新等能力的数据入口。
 */
@Mapper
public interface UserAccountMapper {

    @Select("""
            select id, username, password_hash, display_name, role, subscription_tier, enabled,
                   daily_request_quota, preferred_model, created_at, updated_at
            from user_account
            where id = #{id}
            """)
    @Results(id = "userAccountResultMap", value = {
            @Result(column = "id", property = "id"),
            @Result(column = "username", property = "username"),
            @Result(column = "password_hash", property = "passwordHash"),
            @Result(column = "display_name", property = "displayName"),
            @Result(column = "role", property = "role"),
            @Result(column = "subscription_tier", property = "subscriptionTier"),
            @Result(column = "enabled", property = "enabled"),
            @Result(column = "daily_request_quota", property = "dailyRequestQuota"),
            @Result(column = "preferred_model", property = "preferredModel"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt")
    })
    UserAccount findById(Long id);

    @Select("""
            select id, username, password_hash, display_name, role, subscription_tier, enabled,
                   daily_request_quota, preferred_model, created_at, updated_at
            from user_account
            where username = #{username}
            """)
    @ResultMap("userAccountResultMap")
    UserAccount findByUsername(String username);

    @Select("""
            select id, username, password_hash, display_name, role, subscription_tier, enabled,
                   daily_request_quota, preferred_model, created_at, updated_at
            from user_account
            order by id asc
            """)
    @ResultMap("userAccountResultMap")
    List<UserAccount> findAllByOrderByIdAsc();

    @Select("select count(1) from user_account where username = #{username}")
    int countByUsername(String username);

    @Select("select count(1) from user_account")
    long countAll();

    @Insert("""
            insert into user_account (
                username, password_hash, display_name, role, subscription_tier, enabled,
                daily_request_quota, preferred_model, created_at, updated_at
            ) values (
                #{username}, #{passwordHash}, #{displayName}, #{role}, #{subscriptionTier}, #{enabled},
                #{dailyRequestQuota}, #{preferredModel}, #{createdAt}, #{updatedAt}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserAccount user);

    @Update("""
            update user_account
            set username = #{username},
                password_hash = #{passwordHash},
                display_name = #{displayName},
                role = #{role},
                subscription_tier = #{subscriptionTier},
                enabled = #{enabled},
                daily_request_quota = #{dailyRequestQuota},
                preferred_model = #{preferredModel},
                updated_at = #{updatedAt}
            where id = #{id}
            """)
    int update(UserAccount user);
}
