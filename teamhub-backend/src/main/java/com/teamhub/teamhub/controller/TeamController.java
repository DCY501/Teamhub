package com.teamhub.teamhub.controller;

import com.teamhub.teamhub.config.AuthUser;
import com.teamhub.teamhub.config.SecurityUtil;
import com.teamhub.teamhub.entity.Team;
import com.teamhub.teamhub.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/team")
public class TeamController {

    @Autowired
    private TeamService teamService;

    @GetMapping("/{id}")
    public Team getById(@PathVariable Long id) {
        return teamService.getById(id);
    }

    @PostMapping("/refresh-invite")
    @PreAuthorize("hasRole('CAPTAIN')")
    public Map<String, Object> refreshInvite(@RequestParam Long teamId) {
        AuthUser currentUser = SecurityUtil.getCurrentUser();
        Team team = teamService.getById(teamId);
        if (team == null || !team.getCaptainId().equals(currentUser.getId())) {
            throw new RuntimeException("无权限操作该球队");
        }
        String code = teamService.refreshInviteCode(teamId);
        return Map.of(
            "inviteCode", code,
            "inviteCodeExpiresAt", java.time.LocalDateTime.now().plusDays(7).toString(),
            "message", "邀请码已刷新，7天内有效"
        );
    }

    @PutMapping("/name")
    @PreAuthorize("hasRole('CAPTAIN')")
    public Map<String, String> updateName(@RequestParam Long teamId, @RequestParam String name) {
        AuthUser currentUser = SecurityUtil.getCurrentUser();
        Team team = teamService.getById(teamId);
        if (team == null || !team.getCaptainId().equals(currentUser.getId())) {
            throw new RuntimeException("无权限操作该球队");
        }
        teamService.updateTeamName(teamId, name);
        return Map.of("message", "队名修改成功");
    }

    @PostMapping("/transfer-captain")
    @PreAuthorize("hasRole('CAPTAIN')")
    public Map<String, String> transferCaptain(@RequestParam Long teamId, @RequestParam Long newCaptainUserId) {
        AuthUser currentUser = SecurityUtil.getCurrentUser();
        teamService.transferCaptain(teamId, currentUser.getId(), newCaptainUserId);
        return Map.of("message", "队长传承成功，请重新登录");
    }

    @PostMapping("/disband")
    @PreAuthorize("hasRole('CAPTAIN')")
    public Map<String, String> disband(@RequestParam Long teamId) {
        AuthUser currentUser = SecurityUtil.getCurrentUser();
        teamService.disbandTeam(teamId, currentUser.getId());
        return Map.of("message", "球队已解散，请重新登录");
    }
}
