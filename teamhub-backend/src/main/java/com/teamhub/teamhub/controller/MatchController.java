package com.teamhub.teamhub.controller;

import com.teamhub.teamhub.config.SecurityUtil;
import com.teamhub.teamhub.dto.MatchCreateRequest;
import com.teamhub.teamhub.entity.MatchSet;
import com.teamhub.teamhub.service.MatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/match")
public class MatchController {

    @Autowired
    private MatchService matchService;

    @GetMapping("/list")
    public List<?> list(@RequestParam(required = false) String gameType) {
        return matchService.list(gameType);
    }

    @GetMapping("/{id}")
    public Map<String, Object> detail(@PathVariable Long id) {
        return matchService.getDetail(id);
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('CAPTAIN')")
    public Map<String, String> create(@RequestBody MatchCreateRequest req) {
        req.setCreatedBy(SecurityUtil.getCurrentUserId());
        matchService.create(req);
        return Map.of("message", "创建成功");
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('CAPTAIN')")
    public Map<String, String> updateStatus(@PathVariable Long id, @RequestParam String status) {
        matchService.updateStatus(id, status);
        return Map.of("message", "更新成功");
    }

    @PutMapping("/{id}/notes")
    public Map<String, String> updateNotes(@PathVariable Long id, @RequestBody Map<String, String> body) {
        matchService.updateNotes(id, body.get("notes"));
        return Map.of("message", "备注更新成功");
    }

    @PutMapping("/{id}/sets")
    public Map<String, String> updateSets(@PathVariable Long id, @RequestBody List<MatchSet> sets) {
        matchService.updateSets(id, sets);
        return Map.of("message", "比分更新成功");
    }

    @PutMapping("/{id}/lineup")
    public Map<String, String> updateLineup(@PathVariable Long id, @RequestBody Map<String, String> body) {
        matchService.updateLineup(id, body.get("lineup"));
        return Map.of("message", "首发名单更新成功");
    }

    @PutMapping("/{id}/video")
    public Map<String, String> updateVideoUrl(@PathVariable Long id, @RequestBody Map<String, String> body) {
        matchService.updateVideoUrl(id, body.get("videoUrl"));
        return Map.of("message", "视频链接更新成功");
    }
}
