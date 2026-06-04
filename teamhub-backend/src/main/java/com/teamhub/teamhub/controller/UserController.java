package com.teamhub.teamhub.controller;

import com.teamhub.teamhub.config.SecurityUtil;
import com.teamhub.teamhub.dto.LoginRequest;
import com.teamhub.teamhub.dto.RegisterRequest;
import com.teamhub.teamhub.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody RegisterRequest req) {
        return userService.register(req);
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest req) {
        return userService.login(req);
    }

    @PutMapping("/password")
    public Map<String, String> changePassword(@RequestParam String oldPassword, @RequestParam String newPassword) {
        userService.changePassword(SecurityUtil.getCurrentUserId(), oldPassword, newPassword);
        return Map.of("message", "密码修改成功");
    }

    @PostMapping("/reset-password")
    public Map<String, String> resetPassword(@RequestParam String username, @RequestParam String inviteCode, @RequestParam String newPassword) {
        userService.resetPasswordByInviteCode(username, inviteCode, newPassword);
        return Map.of("message", "密码重置成功，请用新密码登录");
    }
}
