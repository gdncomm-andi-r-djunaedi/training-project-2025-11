package com.marketplace.gateway.command.impl;

import com.marketplace.common.dto.ValidateCredentialsRequest;
import com.marketplace.common.util.JwtUtil;
import com.marketplace.gateway.client.MemberServiceClient;
import com.marketplace.gateway.command.LoginCommand;
import com.marketplace.gateway.dto.LoginRequest;
import com.marketplace.gateway.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginCommandImpl implements LoginCommand {

        private final MemberServiceClient memberServiceClient;
        private final JwtUtil jwtUtil;

        @Override
        public Mono<LoginResponse> execute(LoginRequest request) {
                log.info("Processing login request for email: {}", request.getEmail());

                ValidateCredentialsRequest validateRequest = ValidateCredentialsRequest.builder()
                                .email(request.getEmail())
                                .password(request.getPassword())
                                .build();

                return memberServiceClient.validateCredentials(validateRequest)
                                .map(userDetails -> {
                                        String token = jwtUtil.generateToken(
                                                        userDetails.getId(),
                                                        userDetails.getEmail(),
                                                        userDetails.getRoles());

                                        log.info("JWT token generated successfully for email: {}",
                                                        userDetails.getEmail());

                                        return LoginResponse.builder()
                                                        .token(token)
                                                        .type("Bearer")
                                                        .id(userDetails.getId())
                                                        .email(userDetails.getEmail())
                                                        .build();
                                });
        }
}
