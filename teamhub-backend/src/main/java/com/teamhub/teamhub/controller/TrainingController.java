package com.teamhub.teamhub.controller;

import com.teamhub.teamhub.config.SecurityUtil;
import com.teamhub.teamhub.entity.Training;
import com.teamhub.teamhub.service.TrainingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/training")
public class TrainingController {

    @Autowired
    private TrainingService trainingService;

    @GetMapping("/list")
    public List<Training> list() {
        Long userId = SecurityUtil.getCurrentUserId();
        List<Training> list = trainingService.list();
        LocalDateTime now = LocalDateTime.now();
        for (Training t : list) {
            t.setIsFinished(t.getTrainTime().plusHours(3).isBefore(now));
            t.setSignupStatus(trainingService.getSignupStatus(t.getId(), userId));
        }
        return list;
    }

    @GetMapping("/{id}")
    public Training detail(@PathVariable Long id) {
        Training training = trainingService.getById(id);
        if (training != null) {
            training.setIsFinished(training.getTrainTime().plusHours(3).isBefore(LocalDateTime.now()));
        }
        return training;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('CAPTAIN')")
    public Map<String, String> create(@RequestBody Training training) {
        training.setCreatedBy(SecurityUtil.getCurrentUserId());
        trainingService.create(training);
        return Map.of("message", "创建成功");
    }

    @PostMapping("/{id}/signup")
    public Map<String, String> signup(
        @PathVariable Long id,
        @RequestParam String status,
        @RequestParam(required = false) String reason
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        trainingService.signup(id, userId, status, reason);
        return Map.of("message", "报名成功");
    }

    @GetMapping("/{id}/stats")
    public Map<String, Long> stats(@PathVariable Long id) {
        return trainingService.getSignupStats(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CAPTAIN')")
    public Map<String, String> update(@PathVariable Long id, @RequestBody Training training) {
        training.setId(id);
        trainingService.update(training);
        return Map.of("message", "更新成功");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CAPTAIN')")
    public Map<String, String> delete(@PathVariable Long id) {
        trainingService.delete(id);
        return Map.of("message", "删除成功");
    }
}
