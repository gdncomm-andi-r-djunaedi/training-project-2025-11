package com.blibi.apigateway.service;

import com.blibi.apigateway.dto.LoginRequest;
import com.blibi.apigateway.dto.LoginResponse;
import reactor.core.publisher.Mono;

public interface AuthService {
    Mono<LoginResponse> login(LoginRequest request);

    void logout(String token);
}
