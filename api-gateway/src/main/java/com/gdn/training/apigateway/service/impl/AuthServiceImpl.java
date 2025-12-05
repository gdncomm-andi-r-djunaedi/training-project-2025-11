package com.gdn.training.apigateway.service.impl;

import com.gdn.training.apigateway.dto.AuthResponse;
import com.gdn.training.apigateway.service.AuthService;
import com.gdn.training.apigateway.service.JwtService;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final JwtService jwtService;

    public AuthServiceImpl(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public AuthResponse login(String username, String password) {
        // TODO: Call Member Service to validate credentials
        // For now, we will just generate a token if username is "admin"
        if ("admin".equals(username) && "password".equals(password)) {
            String token = jwtService.generateToken(username);
            return AuthResponse.builder()
                    .accessToken(token)
                    .username(username)
                    .expiresIn(1000 * 60 * 30) // 30 minutes
                    .build();
        } else {
            throw new RuntimeException("Invalid access");
        }
    }

    public void logout(String token) {
        // TODO: Implement logout logic (e.g., blacklist token)
    }
}
