package com.teamhub.teamhub.controller;

import com.teamhub.teamhub.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/test")
    public String test() {
        return "数据库连接正常，用户数量：" + userMapper.selectCount(null);
    }
}