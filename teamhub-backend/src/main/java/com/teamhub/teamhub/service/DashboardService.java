package com.teamhub.teamhub.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.teamhub.teamhub.entity.MatchGame;
import com.teamhub.teamhub.entity.Player;
import com.teamhub.teamhub.entity.PlayerSelfReview;
import com.teamhub.teamhub.entity.PlayerSuggestion;
import com.teamhub.teamhub.entity.TeamSuggestion;
import com.teamhub.teamhub.entity.Training;
import com.teamhub.teamhub.entity.TrainingSignup;
import com.teamhub.teamhub.mapper.MatchGameMapper;
import com.teamhub.teamhub.mapper.PlayerMapper;
import com.teamhub.teamhub.mapper.PlayerSelfReviewMapper;
import com.teamhub.teamhub.mapper.PlayerSuggestionMapper;
import com.teamhub.teamhub.mapper.TeamSuggestionMapper;
import com.teamhub.teamhub.mapper.TrainingMapper;
import com.teamhub.teamhub.mapper.TrainingSignupMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    @Autowired
    private TrainingMapper trainingMapper;

    @Autowired
    private TrainingSignupMapper signupMapper;

    @Autowired
    private MatchGameMapper matchGameMapper;

    @Autowired
    private PlayerSuggestionMapper playerSuggestionMapper;

    @Autowired
    private TeamSuggestionMapper teamSuggestionMapper;

    @Autowired
    private PlayerMapper playerMapper;

    @Autowired
    private PlayerSelfReviewMapper selfReviewMapper;

    // 本周训练（未来7天内，含今天）
    public List<Training> getUpcomingTrainings() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekLater = now.plusDays(7);
        List<Training> list = trainingMapper.selectList(
            new QueryWrapper<Training>()
                .ge("train_time", now)
                .le("train_time", weekLater)
                .orderByAsc("train_time")
        );
        for (Training t : list) {
            t.setIsFinished(t.getTrainTime().plusHours(3).isBefore(now));
        }
        return list;
    }

    // 当前用户对某训练的报名状态
    public String getSignupStatus(Long trainingId, Long userId) {
        TrainingSignup signup = signupMapper.selectOne(
            new QueryWrapper<TrainingSignup>()
                .eq("training_id", trainingId)
                .eq("user_id", userId)
        );
        return signup != null ? signup.getStatus() : null;
    }

    // 最近3场比赛
    public List<MatchGame> getRecentMatches() {
        return matchGameMapper.selectList(
            new QueryWrapper<MatchGame>()
                .orderByDesc("game_time")
                .last("LIMIT 3")
        );
    }

    // 个人出勤率
    public Map<String, Object> getAttendanceRate(Long userId) {
        long totalTrainings = trainingMapper.selectCount(new QueryWrapper<>());
        long attendCount = signupMapper.selectCount(
            new QueryWrapper<TrainingSignup>()
                .eq("user_id", userId)
                .eq("status", "ATTEND")
        );
        double rate = totalTrainings == 0 ? 0.0 : Math.round((double) attendCount / totalTrainings * 1000) / 10.0;

        Map<String, Object> result = new HashMap<>();
        result.put("total", totalTrainings);
        result.put("attended", attendCount);
        result.put("rate", rate);
        return result;
    }

    // 待处理动态：队友给我的 PENDING 建议
    public List<PlayerSuggestion> getPendingPlayerSuggestions(Long targetUserId) {
        return playerSuggestionMapper.selectList(
            new QueryWrapper<PlayerSuggestion>()
                .eq("target_user_id", targetUserId)
                .eq("status", "PENDING")
                .orderByDesc("created_at")
                .last("LIMIT 5")
        );
    }

    // 待处理动态：球队 PENDING 建议（队长视角）
    public List<TeamSuggestion> getPendingTeamSuggestions() {
        return teamSuggestionMapper.selectList(
            new QueryWrapper<TeamSuggestion>()
                .eq("status", "PENDING")
                .orderByDesc("created_at")
                .last("LIMIT 5")
        );
    }

    // 球员个人信息
    public Player getPlayerInfo(Long userId) {
        return playerMapper.selectOne(
            new QueryWrapper<Player>().eq("user_id", userId)
        );
    }

    // 最新自评
    public PlayerSelfReview getLatestSelfReview(Long userId) {
        List<PlayerSelfReview> list = selfReviewMapper.selectList(
            new QueryWrapper<PlayerSelfReview>()
                .eq("user_id", userId)
                .orderByDesc("created_at")
                .last("LIMIT 1")
        );
        return list.isEmpty() ? null : list.get(0);
    }
}
