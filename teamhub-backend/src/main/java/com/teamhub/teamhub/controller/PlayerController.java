package com.teamhub.teamhub.controller;

import com.teamhub.teamhub.config.AuthUser;
import com.teamhub.teamhub.config.SecurityUtil;
import com.teamhub.teamhub.entity.Player;
import com.teamhub.teamhub.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/player")
public class PlayerController {

    @Autowired
    private PlayerService playerService;

    @GetMapping("/list")
    public List<Player> list(@RequestParam(required = false) String status) {
        AuthUser user = SecurityUtil.getCurrentUser();
        return playerService.list(user.getTeamId(), status);
    }

    @GetMapping("/{id}")
    public Player detail(@PathVariable Long id) {
        return playerService.getById(id);
    }

    @PutMapping("/{id}")
    public Map<String, String> update(@PathVariable Long id, @RequestBody Player player) {
        AuthUser currentUser = SecurityUtil.getCurrentUser();
        Player existing = playerService.getById(id);
        if (existing == null) throw new RuntimeException("球员不存在");

        boolean isCaptain = "CAPTAIN".equals(currentUser.getRole());
        boolean isSelf = existing.getUserId().equals(currentUser.getId());
        if (!isCaptain && !isSelf) {
            throw new RuntimeException("无权编辑他人资料");
        }

        // 队长可以修改所有字段，成员只能改自己的部分字段
        if (isCaptain) {
            existing.setName(player.getName());
            existing.setJerseyNumber(player.getJerseyNumber());
            existing.setPosition(player.getPosition());
            existing.setPhotoUrl(player.getPhotoUrl());
            existing.setDegree(player.getDegree());
            existing.setEntryYear(player.getEntryYear());
        } else {
            existing.setJerseyNumber(player.getJerseyNumber());
            existing.setPosition(player.getPosition());
            existing.setPhotoUrl(player.getPhotoUrl());
            existing.setDegree(player.getDegree());
            existing.setEntryYear(player.getEntryYear());
        }
        playerService.update(existing);
        return Map.of("message", "更新成功");
    }

    @PutMapping("/{id}/graduate")
    @PreAuthorize("hasRole('CAPTAIN')")
    public Map<String, String> graduate(@PathVariable Long id) {
        playerService.graduate(id, SecurityUtil.getCurrentUserId());
        return Map.of("message", "已迁移至功勋人物");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CAPTAIN')")
    public Map<String, String> delete(@PathVariable Long id) {
        playerService.deletePlayer(id);
        return Map.of("message", "成员已删除");
    }
}
