package com.teamhub.teamhub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("team")
public class Team {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Long captainId;
    private String inviteCode;
    private LocalDateTime inviteCodeExpiresAt;
    private LocalDateTime createdAt;
}
