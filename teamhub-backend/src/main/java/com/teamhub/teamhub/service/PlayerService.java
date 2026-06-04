package com.teamhub.teamhub.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.teamhub.teamhub.entity.Player;
import com.teamhub.teamhub.entity.User;
import com.teamhub.teamhub.mapper.PlayerMapper;
import com.teamhub.teamhub.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PlayerService {

    @Autowired
    private PlayerMapper playerMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TimelineService timelineService;

    public List<Player> list(Long teamId, String status) {
        QueryWrapper<Player> qw = new QueryWrapper<>();
        qw.eq("team_id", teamId);
        if (status != null && !status.isEmpty()) {
            qw.eq("status", status);
        }
        qw.orderByAsc("jersey_number");
        List<Player> players = playerMapper.selectList(qw);

        // 批量查询用户角色
        List<Long> userIds = players.stream().map(Player::getUserId).collect(Collectors.toList());
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            Map<Long, String> roleMap = users.stream().collect(Collectors.toMap(User::getId, User::getRole));
            for (Player p : players) {
                p.setRole(roleMap.getOrDefault(p.getUserId(), "MEMBER"));
            }
        }
        return players;
    }

    public Player getById(Long id) {
        Player player = playerMapper.selectById(id);
        if (player != null) {
            User user = userMapper.selectById(player.getUserId());
            if (user != null) {
                player.setRole(user.getRole());
            }
        }
        return player;
    }

    public void update(Player player) {
        playerMapper.updateById(player);
    }

    public void graduate(Long playerId, Long createdBy) {
        Player player = playerMapper.selectById(playerId);
        if (player == null) throw new RuntimeException("球员不存在");
        player.setStatus("ALUMNI");
        playerMapper.updateById(player);
        timelineService.autoPlayerGraduated(playerId, player.getName(), createdBy);
    }

    @Transactional
    public void deletePlayer(Long playerId) {
        Player player = playerMapper.selectById(playerId);
        if (player == null) throw new RuntimeException("球员不存在");
        // 先删 player 解除外键引用，再删 user，保证事务原子性
        playerMapper.deleteById(playerId);
        userMapper.deleteById(player.getUserId());
    }
}
