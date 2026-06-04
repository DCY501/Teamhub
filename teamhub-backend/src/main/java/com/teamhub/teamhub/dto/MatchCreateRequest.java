package com.teamhub.teamhub.dto;

import com.teamhub.teamhub.entity.MatchSet;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MatchCreateRequest {
    private String opponent;
    private LocalDateTime gameTime;
    private String location;
    private String gameType;
    private String lineup;
    private String notes;
    private String videoUrl;
    private Long createdBy;
    private List<MatchSet> sets;
}