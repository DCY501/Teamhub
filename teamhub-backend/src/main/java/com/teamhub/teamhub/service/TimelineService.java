package com.teamhub.teamhub.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.teamhub.teamhub.entity.TimelineEvent;
import com.teamhub.teamhub.entity.User;
import com.teamhub.teamhub.mapper.TimelineEventMapper;
import com.teamhub.teamhub.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TimelineService {

    @Autowired
    private TimelineEventMapper timelineEventMapper;

    @Autowired
    private UserMapper userMapper;

    public void createEvent(TimelineEvent event) {
        if (event.getEventTime() == null) {
            event.setEventTime(LocalDateTime.now());
        }
        timelineEventMapper.insert(event);
    }

    public TimelineEvent getById(Long id) {
        return timelineEventMapper.selectById(id);
    }

    public void updateEvent(TimelineEvent event) {
        timelineEventMapper.updateById(event);
    }

    public List<TimelineEvent> listEvents(Long teamId, Integer year, Integer month) {
        QueryWrapper<TimelineEvent> qw = new QueryWrapper<>();
        qw.eq("team_id", teamId);
        if (year != null) {
            qw.apply("YEAR(event_time) = {0}", year);
        }
        if (month != null) {
            qw.apply("MONTH(event_time) = {0}", month);
        }
        qw.orderByDesc("event_time");
        return timelineEventMapper.selectList(qw);
    }

    // ========== 自动事件快捷方法 ==========

    private Long resolveTeamId(Long createdBy) {
        User user = userMapper.selectById(createdBy);
        return user != null ? user.getTeamId() : null;
    }

    public void autoTrainingCreated(Long trainingId, String title, Long createdBy) {
        TimelineEvent e = new TimelineEvent();
        e.setEventType("AUTO");
        e.setTitle("新训练发布：" + title);
        e.setDescription("球队发布了一次新的训练安排");
        e.setRefTrainingId(trainingId);
        e.setCreatedBy(createdBy);
        e.setTeamId(resolveTeamId(createdBy));
        createEvent(e);
    }

    public void autoMatchFinished(Long matchId, String opponent, String gameType, Long createdBy) {
        TimelineEvent e = new TimelineEvent();
        e.setEventType("AUTO");
        e.setTitle("比赛结束：" + gameType + " vs " + opponent);
        e.setDescription("比赛已结束，可在比赛中心查看详细比分与总结");
        e.setRefMatchId(matchId);
        e.setCreatedBy(createdBy);
        e.setTeamId(resolveTeamId(createdBy));
        createEvent(e);
    }

    public void autoPlayerJoined(Long playerId, String playerName, Long createdBy) {
        TimelineEvent e = new TimelineEvent();
        e.setEventType("AUTO");
        e.setTitle("新成员入队：" + playerName);
        e.setDescription("欢迎新队友加入球队");
        e.setRefPlayerId(playerId);
        e.setCreatedBy(createdBy);
        e.setTeamId(resolveTeamId(createdBy));
        createEvent(e);
    }

    public void autoPlayerGraduated(Long playerId, String playerName, Long createdBy) {
        TimelineEvent e = new TimelineEvent();
        e.setEventType("AUTO");
        e.setTitle("功勋退役：" + playerName);
        e.setDescription("该球员已毕业/离队，归档至功勋人物");
        e.setRefPlayerId(playerId);
        e.setCreatedBy(createdBy);
        e.setTeamId(resolveTeamId(createdBy));
        createEvent(e);
    }
}
