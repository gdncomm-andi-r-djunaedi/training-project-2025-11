package com.gdn.training.apigateway.service;

import com.gdn.training.apigateway.dto.AuthResponse;

public interface AuthService {
    AuthResponse login(String username, String password);

    void logout(String token);
}
