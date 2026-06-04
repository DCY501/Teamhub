package com.teamhub.teamhub.controller;

import com.teamhub.teamhub.config.SecurityUtil;
import com.teamhub.teamhub.entity.MatchGame;
import com.teamhub.teamhub.entity.PlayerSuggestion;
import com.teamhub.teamhub.entity.TeamSuggestion;
import com.teamhub.teamhub.entity.Training;
import com.teamhub.teamhub.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping
    public Map<String, Object> getDashboard() {
        Long userId = SecurityUtil.getCurrentUserId();
        boolean isCaptain = SecurityUtil.isCaptain();

        List<Training> trainings = dashboardService.getUpcomingTrainings();
        for (Training t : trainings) {
            String status = dashboardService.getSignupStatus(t.getId(), userId);
            t.setSignupStatus(status);  // 需要 Training 实体有这个字段
        }

        Map<String, Object> result = new HashMap<>();
        result.put("upcomingTrainings", trainings);
        result.put("recentMatches", dashboardService.getRecentMatches());
        result.put("attendance", dashboardService.getAttendanceRate(userId));
        result.put("pendingPlayerSuggestions", dashboardService.getPendingPlayerSuggestions(userId));
        result.put("playerInfo", dashboardService.getPlayerInfo(userId));
        result.put("latestSelfReview", dashboardService.getLatestSelfReview(userId));
        if (isCaptain) {
            result.put("pendingTeamSuggestions", dashboardService.getPendingTeamSuggestions());
        }
        return result;
    }
}
