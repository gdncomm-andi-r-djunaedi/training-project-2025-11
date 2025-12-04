package com.marketplace.gateway.service;

import com.marketplace.common.dto.UserDetailsResponse;
import com.marketplace.common.dto.ValidateCredentialsRequest;
import com.marketplace.common.util.JwtUtil;
import com.marketplace.gateway.client.MemberServiceClient;
import com.marketplace.gateway.dto.LoginRequest;
import com.marketplace.gateway.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Authentication service for API Gateway
 * Handles JWT generation after credential validation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberServiceClient memberServiceClient;
    private final JwtUtil jwtUtil;

    /**
     * Authenticate user and generate JWT token
     */
    public Mono<LoginResponse> login(LoginRequest request) {
        log.info("Processing login request for user: {}", request.getUsername());

        // Call Member Service to validate credentials
        ValidateCredentialsRequest validateRequest = ValidateCredentialsRequest.builder()
                .username(request.getUsername())
                .password(request.getPassword())
                .build();

        return memberServiceClient.validateCredentials(validateRequest)
                .map(userDetails -> {
                    // Generate JWT token with user details
                    String token = jwtUtil.generateToken(
                            userDetails.getId(),
                            userDetails.getUsername(),
                            userDetails.getRoles());

                    log.info("JWT token generated successfully for user: {}", userDetails.getUsername());

                    return LoginResponse.builder()
                            .token(token)
                            .type("Bearer")
                            .id(userDetails.getId())
                            .username(userDetails.getUsername())
                            .email(userDetails.getEmail())
                            .build();
                });
    }
}
