package com.teamhub.teamhub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("timeline_event")
public class TimelineEvent {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String eventType;      // AUTO / MANUAL
    private String title;
    private String description;
    private String imageUrls;      // JSON数组，最多3个图片链接
    private String albumUrl;
    private Long refTrainingId;
    private Long refMatchId;
    private Long refPlayerId;
    private Long teamId;
    private LocalDateTime eventTime;
    private Long createdBy;
    private LocalDateTime createdAt;
}
