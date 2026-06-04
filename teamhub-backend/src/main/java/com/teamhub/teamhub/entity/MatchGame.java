package com.teamhub.teamhub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("match_game")
public class MatchGame {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String opponent;
    private LocalDateTime gameTime;
    private String location;
    private String gameType;
    private String status;
    private String lineup;
    private String notes;
    private String videoUrl;
    private Long createdBy;
    private LocalDateTime createdAt;
}