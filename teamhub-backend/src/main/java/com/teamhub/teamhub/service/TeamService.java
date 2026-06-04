package com.teamhub.teamhub.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.teamhub.teamhub.entity.Player;
import com.teamhub.teamhub.entity.Team;
import com.teamhub.teamhub.entity.TimelineEvent;
import com.teamhub.teamhub.entity.User;
import com.teamhub.teamhub.mapper.PlayerMapper;
import com.teamhub.teamhub.mapper.TeamMapper;
import com.teamhub.teamhub.mapper.TimelineEventMapper;
import com.teamhub.teamhub.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class TeamService {

    @Autowired
    private TeamMapper teamMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PlayerMapper playerMapper;

    @Autowired
    private TimelineEventMapper timelineEventMapper;

    public Team createTeam(String name, Long captainId) {
        Team team = new Team();
        team.setName(name);
        team.setCaptainId(captainId);
        team.setInviteCode(generateUniqueInviteCode());
        team.setInviteCodeExpiresAt(LocalDateTime.now().plusDays(7));
        team.setCreatedAt(LocalDateTime.now());
        teamMapper.insert(team);
        return team;
    }

    public Team getByInviteCode(String inviteCode) {
        return teamMapper.selectOne(
            new QueryWrapper<Team>()
                .eq("invite_code", inviteCode)
                .gt("invite_code_expires_at", LocalDateTime.now())
        );
    }

    public Team getById(Long id) {
        return teamMapper.selectById(id);
    }

    public String refreshInviteCode(Long teamId) {
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new RuntimeException("球队不存在");
        }
        team.setInviteCode(generateUniqueInviteCode());
        team.setInviteCodeExpiresAt(LocalDateTime.now().plusDays(7));
        teamMapper.updateById(team);
        return team.getInviteCode();
    }

    public void updateTeamName(Long teamId, String name) {
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new RuntimeException("球队不存在");
        }
        team.setName(name);
        teamMapper.updateById(team);
    }

    private String generateUniqueInviteCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        String code;
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                sb.append(chars.charAt(random.nextInt(chars.length())));
            }
            code = sb.toString();
        } while (teamMapper.selectCount(new QueryWrapper<Team>().eq("invite_code", code)) > 0);
        return code;
    }

    @Transactional
    public void transferCaptain(Long teamId, Long currentCaptainId, Long newCaptainUserId) {
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new RuntimeException("球队不存在");
        }
        if (!team.getCaptainId().equals(currentCaptainId)) {
            throw new RuntimeException("只有队长可以传承队长身份");
        }
        if (currentCaptainId.equals(newCaptainUserId)) {
            throw new RuntimeException("不能传承给自己");
        }

        User newCaptain = userMapper.selectById(newCaptainUserId);
        if (newCaptain == null || !teamId.equals(newCaptain.getTeamId())) {
            throw new RuntimeException("该成员不属于本球队");
        }

        // 更新球队队长
        team.setCaptainId(newCaptainUserId);
        teamMapper.updateById(team);

        // 老队长降级为普通成员
        User oldCaptain = userMapper.selectById(currentCaptainId);
        oldCaptain.setRole("MEMBER");
        userMapper.updateById(oldCaptain);

        // 新队长升级
        newCaptain.setRole("CAPTAIN");
        userMapper.updateById(newCaptain);
    }

    @Transactional
    public void disbandTeam(Long teamId, Long captainId) {
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new RuntimeException("球队不存在");
        }
        if (!team.getCaptainId().equals(captainId)) {
            throw new RuntimeException("只有队长可以解散球队");
        }

        // 检查是否还有其他成员
        long memberCount = userMapper.selectCount(new QueryWrapper<User>().eq("team_id", teamId));
        if (memberCount > 1) {
            throw new RuntimeException("球队还有其他成员，请先删除所有成员或传承队长身份后再解散");
        }

        // 删除该球队的所有时光轴事件
        timelineEventMapper.delete(new QueryWrapper<TimelineEvent>().eq("team_id", teamId));

        // 先删 player 解除外键引用，再删 user，最后删 team
        Player captainPlayer = playerMapper.selectOne(new QueryWrapper<Player>().eq("user_id", captainId));
        if (captainPlayer != null) {
            playerMapper.deleteById(captainPlayer.getId());
        }
        userMapper.deleteById(captainId);
        teamMapper.deleteById(teamId);
    }
}
