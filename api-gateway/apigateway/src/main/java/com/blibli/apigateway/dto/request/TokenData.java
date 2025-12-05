package com.blibli.apigateway.dto.request;

import java.time.LocalDateTime;

public class TokenData {
    private String email;
    private String token;
    private LocalDateTime expiry;

    public TokenData() {
    }

    public TokenData(String email, String token, LocalDateTime expiry) {
        this.email = email;
        this.token = token;
        this.expiry = expiry;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getExpiry() {
        return expiry;
    }

    public void setExpiry(LocalDateTime expiry) {
        this.expiry = expiry;
    }
}


