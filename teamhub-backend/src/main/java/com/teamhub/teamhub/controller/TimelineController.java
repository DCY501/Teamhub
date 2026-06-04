package com.teamhub.teamhub.controller;

import com.teamhub.teamhub.config.AuthUser;
import com.teamhub.teamhub.config.SecurityUtil;
import com.teamhub.teamhub.entity.TimelineEvent;
import com.teamhub.teamhub.service.TimelineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/timeline")
public class TimelineController {

    @Autowired
    private TimelineService timelineService;

    @GetMapping("/list")
    public List<TimelineEvent> listEvents(
        @RequestParam(required = false) Integer year,
        @RequestParam(required = false) Integer month
    ) {
        AuthUser user = SecurityUtil.getCurrentUser();
        return timelineService.listEvents(user.getTeamId(), year, month);
    }

    @GetMapping("/{id}")
    public TimelineEvent getEvent(@PathVariable Long id) {
        return timelineService.getById(id);
    }

    @PutMapping("/{id}")
    public Map<String, String> updateEvent(@PathVariable Long id, @RequestBody TimelineEvent event) {
        TimelineEvent existing = timelineService.getById(id);
        if (existing == null) throw new RuntimeException("事件不存在");
        if ("AUTO".equals(existing.getEventType())) {
            throw new RuntimeException("自动事件不可编辑");
        }
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (!currentUserId.equals(existing.getCreatedBy()) && !SecurityUtil.isCaptain()) {
            throw new RuntimeException("无权编辑他人发布的事件");
        }
        existing.setTitle(event.getTitle());
        existing.setDescription(event.getDescription());
        existing.setEventTime(event.getEventTime());
        existing.setImageUrls(event.getImageUrls());
        existing.setAlbumUrl(event.getAlbumUrl());
        existing.setRefTrainingId(event.getRefTrainingId());
        existing.setRefMatchId(event.getRefMatchId());
        existing.setRefPlayerId(event.getRefPlayerId());
        timelineService.updateEvent(existing);
        return Map.of("message", "事件更新成功");
    }

    @PostMapping
    public Map<String, String> createManualEvent(@RequestBody TimelineEvent event) {
        AuthUser user = SecurityUtil.getCurrentUser();
        event.setEventType("MANUAL");
        event.setCreatedBy(user.getId());
        event.setTeamId(user.getTeamId());
        timelineService.createEvent(event);
        return Map.of("message", "事件发布成功");
    }
}
