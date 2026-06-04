package com.teamhub.teamhub.config;

public class AuthUser {
    private Long id;
    private String username;
    private String role;
    private Long teamId;

    public AuthUser(Long id, String username, String role, Long teamId) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.teamId = teamId;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public Long getTeamId() { return teamId; }
}
