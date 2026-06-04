package com.teamhub.teamhub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("player_self_review")
public class PlayerSelfReview {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String category;       // 技术/体能/意识/态度/配合
    private String currentProblem;
    private String improvementGoal;
    private String expectedTraining;
    private LocalDateTime createdAt;
}
