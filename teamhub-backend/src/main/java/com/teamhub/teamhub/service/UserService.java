package com.teamhub.teamhub.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.teamhub.teamhub.config.JwtUtil;
import com.teamhub.teamhub.dto.LoginRequest;
import com.teamhub.teamhub.dto.RegisterRequest;
import com.teamhub.teamhub.entity.Player;
import com.teamhub.teamhub.entity.Team;
import com.teamhub.teamhub.entity.User;
import com.teamhub.teamhub.mapper.PlayerMapper;
import com.teamhub.teamhub.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PlayerMapper playerMapper;

    @Autowired
    private TeamService teamService;

    @Autowired
    private TimelineService timelineService;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public Map<String, Object> register(RegisterRequest req) {
        if (req.getTeamName() != null && !req.getTeamName().isEmpty()) {
            return registerCaptain(req);
        } else if (req.getInviteCode() != null && !req.getInviteCode().isEmpty()) {
            return registerMember(req);
        } else {
            throw new RuntimeException("请填写球队名称或邀请码");
        }
    }

    private Map<String, Object> registerCaptain(RegisterRequest req) {
        if (userMapper.selectCount(new QueryWrapper<User>().eq("username", req.getUsername())) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole("CAPTAIN");
        userMapper.insert(user);

        Team team = teamService.createTeam(req.getTeamName(), user.getId());

        user.setTeamId(team.getId());
        userMapper.updateById(user);

        Player player = new Player();
        player.setUserId(user.getId());
        player.setTeamId(team.getId());
        player.setName(req.getName());
        player.setEntryYear(req.getEntryYear());
        player.setJerseyNumber(req.getJerseyNumber());
        player.setPosition(req.getPosition());
        player.setDegree(req.getDegree());
        player.setStatus("ACTIVE");
        playerMapper.insert(player);
        timelineService.autoPlayerJoined(player.getId(), player.getName(), user.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("message", "注册成功");
        result.put("role", "CAPTAIN");
        result.put("inviteCode", team.getInviteCode());
        result.put("inviteCodeExpiresAt", team.getInviteCodeExpiresAt());
        return result;
    }

    private Map<String, Object> registerMember(RegisterRequest req) {
        Team team = teamService.getByInviteCode(req.getInviteCode());
        if (team == null) {
            throw new RuntimeException("邀请码无效或已过期");
        }

        if (userMapper.selectCount(new QueryWrapper<User>().eq("username", req.getUsername())) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole("MEMBER");
        user.setTeamId(team.getId());
        userMapper.insert(user);

        Player player = new Player();
        player.setUserId(user.getId());
        player.setTeamId(team.getId());
        player.setName(req.getName());
        player.setEntryYear(req.getEntryYear());
        player.setJerseyNumber(req.getJerseyNumber());
        player.setPosition(req.getPosition());
        player.setDegree(req.getDegree());
        player.setStatus("ACTIVE");
        playerMapper.insert(player);
        timelineService.autoPlayerJoined(player.getId(), player.getName(), user.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("message", "注册成功");
        result.put("role", "MEMBER");
        result.put("teamName", team.getName());
        return result;
    }

    public Map<String, Object> login(LoginRequest req) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", req.getUsername()));
        if (user == null || !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole(), user.getTeamId());

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("role", user.getRole());
        userInfo.put("teamId", user.getTeamId());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", userInfo);
        return result;
    }

    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new RuntimeException("用户不存在");
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    public void resetPasswordByInviteCode(String username, String inviteCode, String newPassword) {
        Team team = teamService.getByInviteCode(inviteCode);
        if (team == null) {
            throw new RuntimeException("邀请码无效或已过期");
        }
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (user == null) {
            throw new RuntimeException("用户名不存在");
        }
        if (!user.getTeamId().equals(team.getId())) {
            throw new RuntimeException("该用户不属于此球队");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }
}
