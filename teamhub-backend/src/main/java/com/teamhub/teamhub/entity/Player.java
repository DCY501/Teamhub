package com.teamhub.teamhub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("player")
public class Player {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String name;
    private Integer jerseyNumber;
    private String position;
    private Integer entryYear;
    private String photoUrl;
    private String status;
    private String degree;    // 本科 / 硕士 / 博士
    private Long teamId;
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private String role;
}