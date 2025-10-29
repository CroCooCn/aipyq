package com.aipyq.friendapp.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("copy_history")
public class CopyHistoryEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String userId;
    private String clientKey;
    private String imageId;
    private String instruction;
    private String copyText;
    private String source;
    private Integer sequenceNo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
