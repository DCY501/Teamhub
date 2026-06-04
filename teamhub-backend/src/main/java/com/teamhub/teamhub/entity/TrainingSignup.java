package com.teamhub.teamhub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("training_signup")
public class TrainingSignup {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long trainingId;
    private Long userId;
    private String status;  // ATTEND / ABSENT / PENDING
    private String reason;
}