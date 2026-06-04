package com.teamhub.teamhub.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String name;
    private Integer entryYear;
    private Integer jerseyNumber;
    private String position;
    private String degree;      // 本科 / 硕士 / 博士
    private String teamName;    // 队长注册时填写
    private String inviteCode;  // 球员注册时填写
}