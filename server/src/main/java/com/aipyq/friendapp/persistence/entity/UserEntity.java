package com.aipyq.friendapp.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("users")
public class UserEntity {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String phone;
    private String planId;
    private Integer remainingQuota;
    private String authSecret;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
