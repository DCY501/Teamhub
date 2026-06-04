package com.teamhub.teamhub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("team_suggestion_like")
public class TeamSuggestionLike {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long suggestionId;
    private LocalDateTime createdAt;
}
