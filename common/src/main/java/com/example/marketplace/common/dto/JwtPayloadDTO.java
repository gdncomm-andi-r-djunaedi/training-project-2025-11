package com.example.marketplace.common.dto;

public class JwtPayloadDTO {
    private String userId;
    private String username;

    public JwtPayloadDTO() {}
    public JwtPayloadDTO(String userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
