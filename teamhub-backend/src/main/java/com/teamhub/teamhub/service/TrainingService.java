package com.teamhub.teamhub.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.teamhub.teamhub.entity.Training;
import com.teamhub.teamhub.entity.TrainingSignup;
import com.teamhub.teamhub.mapper.TrainingMapper;
import com.teamhub.teamhub.mapper.TrainingSignupMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TrainingService {

    @Autowired
    private TrainingMapper trainingMapper;

    @Autowired
    private TrainingSignupMapper signupMapper;

    public List<Training> list() {
        return trainingMapper.selectList(new QueryWrapper<Training>().orderByDesc("train_time"));
    }

    public Training getById(Long id) {
        return trainingMapper.selectById(id);
    }

    @Autowired
    private TimelineService timelineService;

    @Transactional
    public void create(Training training) {
        trainingMapper.insert(training);
        timelineService.autoTrainingCreated(training.getId(), training.getTitle(), training.getCreatedBy());
    }

    public void signup(Long trainingId, Long userId, String status, String reason) {
        Training training = trainingMapper.selectById(trainingId);
        if (training == null) throw new RuntimeException("训练不存在");
        if (training.getTrainTime().plusHours(3).isBefore(LocalDateTime.now())) {
            throw new RuntimeException("训练已结束，无法修改报名状态");
        }

        TrainingSignup existing = signupMapper.selectOne(
            new QueryWrapper<TrainingSignup>()
                .eq("training_id", trainingId)
                .eq("user_id", userId)
        );
        if (existing != null) {
            existing.setStatus(status);
            existing.setReason(reason);
            signupMapper.updateById(existing);
        } else {
            TrainingSignup signup = new TrainingSignup();
            signup.setTrainingId(trainingId);
            signup.setUserId(userId);
            signup.setStatus(status);
            signup.setReason(reason);
            signupMapper.insert(signup);
        }
    }

    public String getSignupStatus(Long trainingId, Long userId) {
        TrainingSignup signup = signupMapper.selectOne(
            new QueryWrapper<TrainingSignup>()
                .eq("training_id", trainingId)
                .eq("user_id", userId)
        );
        return signup != null ? signup.getStatus() : null;
    }

    public Map<String, Long> getSignupStats(Long trainingId) {
        Map<String, Long> stats = new HashMap<>();
        stats.put("attend", signupMapper.selectCount(new QueryWrapper<TrainingSignup>().eq("training_id", trainingId).eq("status", "ATTEND")));
        stats.put("absent", signupMapper.selectCount(new QueryWrapper<TrainingSignup>().eq("training_id", trainingId).eq("status", "ABSENT")));
        stats.put("pending", signupMapper.selectCount(new QueryWrapper<TrainingSignup>().eq("training_id", trainingId).eq("status", "PENDING")));
        return stats;
    }

    public void update(Training training) {
        trainingMapper.updateById(training);
    }

    @Transactional
    public void delete(Long id) {
        signupMapper.delete(new QueryWrapper<TrainingSignup>().eq("training_id", id));
        trainingMapper.deleteById(id);
    }
}