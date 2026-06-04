package com.teamhub.teamhub.controller;

import com.teamhub.teamhub.config.AuthUser;
import com.teamhub.teamhub.config.SecurityUtil;
import com.teamhub.teamhub.entity.PlayerSelfReview;
import com.teamhub.teamhub.entity.PlayerSuggestion;
import com.teamhub.teamhub.entity.TeamSuggestion;
import com.teamhub.teamhub.service.DevelopmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/development")
public class DevelopmentController {

    @Autowired
    private DevelopmentService developmentService;

    // ========== 球员自评 ==========
    @PostMapping("/self-review")
    public Map<String, String> createSelfReview(@RequestBody PlayerSelfReview review) {
        review.setUserId(SecurityUtil.getCurrentUserId());
        developmentService.createSelfReview(review);
        return Map.of("message", "自评提交成功");
    }

    @GetMapping("/self-review/list")
    public List<PlayerSelfReview> getSelfReviews(@RequestParam(required = false) Long userId) {
        Long targetId = userId != null ? userId : SecurityUtil.getCurrentUserId();
        return developmentService.getSelfReviews(targetId);
    }

    @GetMapping("/self-review/all")
    public List<Map<String, Object>> getAllSelfReviews() {
        AuthUser user = SecurityUtil.getCurrentUser();
        if (user == null || user.getTeamId() == null) {
            return List.of();
        }
        return developmentService.getAllSelfReviews(user.getTeamId());
    }

    // ========== 个人建议 ==========
    @PostMapping("/player-suggestion")
    public Map<String, String> createPlayerSuggestion(@RequestBody PlayerSuggestion suggestion) {
        suggestion.setFromUserId(SecurityUtil.getCurrentUserId());
        developmentService.createPlayerSuggestion(suggestion);
        return Map.of("message", "建议提交成功");
    }

    @GetMapping("/player-suggestion/list")
    public List<PlayerSuggestion> getPlayerSuggestions(@RequestParam Long targetUserId) {
        return developmentService.getPlayerSuggestions(targetUserId, SecurityUtil.getCurrentUserId());
    }

    @PostMapping("/player-suggestion/{id}/like")
    public Map<String, String> likePlayerSuggestion(@PathVariable Long id) {
        developmentService.likePlayerSuggestion(id, SecurityUtil.getCurrentUserId());
        return Map.of("message", "操作成功");
    }

    @PutMapping("/player-suggestion/{id}/status")
    public Map<String, String> updatePlayerSuggestionStatus(@PathVariable Long id, @RequestParam String status) {
        developmentService.updatePlayerSuggestionStatus(id, status);
        return Map.of("message", "状态更新成功");
    }

    @PostMapping("/player-suggestion/{id}/report")
    public Map<String, String> reportPlayerSuggestion(@PathVariable Long id) {
        developmentService.reportPlayerSuggestion(id);
        return Map.of("message", "已举报");
    }

    @GetMapping("/player-suggestion/reported")
    @PreAuthorize("hasRole('CAPTAIN')")
    public List<PlayerSuggestion> getReportedSuggestions() {
        return developmentService.getReportedSuggestions();
    }

    @PostMapping("/player-suggestion/{id}/ignore")
    @PreAuthorize("hasRole('CAPTAIN')")
    public Map<String, String> ignoreReport(@PathVariable Long id) {
        developmentService.ignoreReport(id);
        return Map.of("message", "已忽略举报");
    }

    @DeleteMapping("/player-suggestion/{id}")
    @PreAuthorize("hasRole('CAPTAIN')")
    public Map<String, String> deletePlayerSuggestion(@PathVariable Long id) {
        developmentService.deletePlayerSuggestion(id);
        return Map.of("message", "建议已删除");
    }

    // ========== 球队建议 ==========
    @PostMapping("/team-suggestion")
    public Map<String, String> createTeamSuggestion(@RequestBody TeamSuggestion suggestion) {
        suggestion.setFromUserId(SecurityUtil.getCurrentUserId());
        developmentService.createTeamSuggestion(suggestion);
        return Map.of("message", "建议提交成功");
    }

    @GetMapping("/team-suggestion/list")
    public List<TeamSuggestion> getTeamSuggestions(
        @RequestParam(required = false) String category,
        @RequestParam(required = false) String status
    ) {
        return developmentService.getTeamSuggestions(category, status, SecurityUtil.getCurrentUserId());
    }

    @PostMapping("/team-suggestion/{id}/like")
    public Map<String, String> likeTeamSuggestion(@PathVariable Long id) {
        developmentService.likeTeamSuggestion(id, SecurityUtil.getCurrentUserId());
        return Map.of("message", "操作成功");
    }

    @PutMapping("/team-suggestion/{id}/reply")
    public Map<String, String> replyTeamSuggestion(
        @PathVariable Long id,
        @RequestParam String status,
        @RequestParam(required = false) String reply
    ) {
        developmentService.replyTeamSuggestion(id, status, reply);
        return Map.of("message", "回应成功");
    }
}
