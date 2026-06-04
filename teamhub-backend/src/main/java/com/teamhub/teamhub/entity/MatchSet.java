package com.teamhub.teamhub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("match_set")
public class MatchSet {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long matchId;
    private Integer setNumber;
    private Integer ourScore;
    private Integer oppScore;
}