package com.teamhub.teamhub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("training")
public class Training {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String location;
    private LocalDateTime trainTime;
    private String content;
    private String videoUrl;
    private Long createdBy;
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private String signupStatus;

    @TableField(exist = false)
    private Boolean isFinished;
}