package com.teamhub.teamhub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("team_suggestion")
public class TeamSuggestion {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long fromUserId;
    private String category;    // 训练内容/时间安排/战术方向/装备后勤/其他
    private String urgency;     // NORMAL/URGENT
    private String content;
    private String status;      // PENDING/ADOPTED/REJECTED/DONE
    private String captainReply;
    private Integer likes;
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private Boolean likedByMe;
}
