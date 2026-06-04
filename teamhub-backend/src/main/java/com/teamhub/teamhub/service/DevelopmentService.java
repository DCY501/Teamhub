package com.teamhub.teamhub.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.teamhub.teamhub.entity.Player;
import com.teamhub.teamhub.entity.PlayerSelfReview;
import com.teamhub.teamhub.entity.PlayerSuggestion;
import com.teamhub.teamhub.entity.PlayerSuggestionLike;
import com.teamhub.teamhub.entity.TeamSuggestion;
import com.teamhub.teamhub.entity.TeamSuggestionLike;
import com.teamhub.teamhub.mapper.PlayerMapper;
import com.teamhub.teamhub.mapper.PlayerSelfReviewMapper;
import com.teamhub.teamhub.mapper.PlayerSuggestionLikeMapper;
import com.teamhub.teamhub.mapper.PlayerSuggestionMapper;
import com.teamhub.teamhub.mapper.TeamSuggestionLikeMapper;
import com.teamhub.teamhub.mapper.TeamSuggestionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DevelopmentService {

    @Autowired
    private PlayerSelfReviewMapper selfReviewMapper;

    @Autowired
    private PlayerSuggestionMapper playerSuggestionMapper;

    @Autowired
    private TeamSuggestionMapper teamSuggestionMapper;

    @Autowired
    private PlayerMapper playerMapper;

    @Autowired
    private PlayerSuggestionLikeMapper playerSuggestionLikeMapper;

    @Autowired
    private TeamSuggestionLikeMapper teamSuggestionLikeMapper;

    // ========== 球员自评 ==========
    public void createSelfReview(PlayerSelfReview review) {
        selfReviewMapper.insert(review);
    }

    public List<PlayerSelfReview> getSelfReviews(Long userId) {
        return selfReviewMapper.selectList(
            new QueryWrapper<PlayerSelfReview>().eq("user_id", userId).orderByDesc("created_at")
        );
    }

    public List<Map<String, Object>> getAllSelfReviews(Long teamId) {
        List<Player> players = playerMapper.selectList(
            new QueryWrapper<Player>().eq("team_id", teamId).eq("status", "ACTIVE")
        );
        List<Map<String, Object>> result = new ArrayList<>();
        for (Player p : players) {
            Map<String, Object> item = new HashMap<>();
            item.put("player", p);
            List<PlayerSelfReview> reviews = selfReviewMapper.selectList(
                new QueryWrapper<PlayerSelfReview>()
                    .eq("user_id", p.getUserId())
                    .orderByDesc("created_at")
            );
            if (!reviews.isEmpty()) {
                item.put("latestReview", reviews.get(0));
            }
            item.put("reviewCount", reviews.size());
            result.add(item);
        }
        return result;
    }

    // ========== 个人建议 ==========
    public void createPlayerSuggestion(PlayerSuggestion suggestion) {
        suggestion.setStatus("PENDING");
        suggestion.setLikes(0);
        suggestion.setIsReported(false);
        playerSuggestionMapper.insert(suggestion);
    }

    public List<PlayerSuggestion> getPlayerSuggestions(Long targetUserId, Long currentUserId) {
        List<PlayerSuggestion> list = playerSuggestionMapper.selectList(
            new QueryWrapper<PlayerSuggestion>().eq("target_user_id", targetUserId).orderByDesc("created_at")
        );
        if (currentUserId != null && !list.isEmpty()) {
            List<Long> suggestionIds = list.stream().map(PlayerSuggestion::getId).toList();
            List<PlayerSuggestionLike> likes = playerSuggestionLikeMapper.selectList(
                new QueryWrapper<PlayerSuggestionLike>()
                    .eq("user_id", currentUserId)
                    .in("suggestion_id", suggestionIds)
            );
            List<Long> likedIds = likes.stream().map(PlayerSuggestionLike::getSuggestionId).toList();
            for (PlayerSuggestion s : list) {
                s.setLikedByMe(likedIds.contains(s.getId()));
            }
        }
        return list;
    }

    public void likePlayerSuggestion(Long id, Long userId) {
        PlayerSuggestion s = playerSuggestionMapper.selectById(id);
        if (s == null) return;

        PlayerSuggestionLike existing = playerSuggestionLikeMapper.selectOne(
            new QueryWrapper<PlayerSuggestionLike>()
                .eq("user_id", userId)
                .eq("suggestion_id", id)
        );

        if (existing != null) {
            // 取消点赞
            playerSuggestionLikeMapper.deleteById(existing.getId());
            s.setLikes(Math.max(0, s.getLikes() - 1));
        } else {
            // 点赞
            PlayerSuggestionLike like = new PlayerSuggestionLike();
            like.setUserId(userId);
            like.setSuggestionId(id);
            playerSuggestionLikeMapper.insert(like);
            s.setLikes(s.getLikes() + 1);
        }
        playerSuggestionMapper.updateById(s);
    }

    public void updatePlayerSuggestionStatus(Long id, String status) {
        PlayerSuggestion s = playerSuggestionMapper.selectById(id);
        if (s != null) {
            s.setStatus(status);
            playerSuggestionMapper.updateById(s);
        }
    }

    public void reportPlayerSuggestion(Long id) {
        PlayerSuggestion s = playerSuggestionMapper.selectById(id);
        if (s != null) {
            s.setIsReported(true);
            playerSuggestionMapper.updateById(s);
        }
    }

    public List<PlayerSuggestion> getReportedSuggestions() {
        return playerSuggestionMapper.selectList(
            new QueryWrapper<PlayerSuggestion>()
                .eq("is_reported", true)
                .orderByDesc("created_at")
        );
    }

    public void ignoreReport(Long id) {
        PlayerSuggestion s = playerSuggestionMapper.selectById(id);
        if (s != null) {
            s.setIsReported(false);
            playerSuggestionMapper.updateById(s);
        }
    }

    public void deletePlayerSuggestion(Long id) {
        playerSuggestionMapper.deleteById(id);
    }

    // ========== 球队建议 ==========
    public void createTeamSuggestion(TeamSuggestion suggestion) {
        suggestion.setStatus("PENDING");
        suggestion.setLikes(0);
        teamSuggestionMapper.insert(suggestion);
    }

    public List<TeamSuggestion> getTeamSuggestions(String category, String status, Long currentUserId) {
        QueryWrapper<TeamSuggestion> qw = new QueryWrapper<>();
        if (category != null && !category.isEmpty()) {
            qw.eq("category", category);
        }
        if (status != null && !status.isEmpty()) {
            qw.eq("status", status);
        }
        qw.orderByDesc("created_at");
        List<TeamSuggestion> list = teamSuggestionMapper.selectList(qw);

        if (currentUserId != null && !list.isEmpty()) {
            List<Long> suggestionIds = list.stream().map(TeamSuggestion::getId).toList();
            List<TeamSuggestionLike> likes = teamSuggestionLikeMapper.selectList(
                new QueryWrapper<TeamSuggestionLike>()
                    .eq("user_id", currentUserId)
                    .in("suggestion_id", suggestionIds)
            );
            List<Long> likedIds = likes.stream().map(TeamSuggestionLike::getSuggestionId).toList();
            for (TeamSuggestion s : list) {
                s.setLikedByMe(likedIds.contains(s.getId()));
            }
        }
        return list;
    }

    public void likeTeamSuggestion(Long id, Long userId) {
        TeamSuggestion s = teamSuggestionMapper.selectById(id);
        if (s == null) return;

        TeamSuggestionLike existing = teamSuggestionLikeMapper.selectOne(
            new QueryWrapper<TeamSuggestionLike>()
                .eq("user_id", userId)
                .eq("suggestion_id", id)
        );

        if (existing != null) {
            // 取消点赞
            teamSuggestionLikeMapper.deleteById(existing.getId());
            s.setLikes(Math.max(0, s.getLikes() - 1));
        } else {
            // 点赞
            TeamSuggestionLike like = new TeamSuggestionLike();
            like.setUserId(userId);
            like.setSuggestionId(id);
            teamSuggestionLikeMapper.insert(like);
            s.setLikes(s.getLikes() + 1);
        }
        teamSuggestionMapper.updateById(s);
    }

    public void replyTeamSuggestion(Long id, String status, String reply) {
        TeamSuggestion s = teamSuggestionMapper.selectById(id);
        if (s != null) {
            s.setStatus(status);
            s.setCaptainReply(reply);
            teamSuggestionMapper.updateById(s);
        }
    }
}
