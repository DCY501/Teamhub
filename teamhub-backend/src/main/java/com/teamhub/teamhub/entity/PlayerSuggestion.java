package com.teamhub.teamhub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("player_suggestion")
public class PlayerSuggestion {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long targetUserId;
    private Long fromUserId;
    private Boolean isAnonymous;
    private String category;    // 技术/体能/意识/态度/配合
    private String content;
    private String status;      // PENDING/ADOPTED/TRYING/SKIP
    private Integer likes;
    private Boolean isReported;
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private Boolean likedByMe;
}
