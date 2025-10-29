package com.aipyq.friendapp.persistence.mapper;

import com.aipyq.friendapp.persistence.entity.UserEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {

    @Select("SELECT id, phone, plan_id AS planId, remaining_quota AS remainingQuota, auth_secret AS authSecret, created_at AS createdAt, updated_at AS updatedAt FROM users WHERE phone = #{phone} LIMIT 1")
    UserEntity findByPhone(@Param("phone") String phone);

    @Update("UPDATE users SET remaining_quota = remaining_quota - #{cost}, updated_at = CURRENT_TIMESTAMP WHERE id = #{id} AND remaining_quota >= #{cost}")
    int decreaseQuota(@Param("id") String id, @Param("cost") int cost);

    @Update("UPDATE users SET remaining_quota = remaining_quota + #{points}, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int increaseQuota(@Param("id") String id, @Param("points") int points);

    @Update("UPDATE users SET auth_secret = #{secret}, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updateAuthSecret(@Param("id") String id, @Param("secret") String secret);
}
