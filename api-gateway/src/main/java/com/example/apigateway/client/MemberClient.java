package com.example.apigateway.client;

import com.example.apigateway.dto.LoginRequest;
import com.example.apigateway.dto.MemberValidationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class MemberClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${member.service.base-url:http://localhost:8081}")
    private String memberServiceBaseUrl;

    public Mono<MemberValidationResponse> validateCredentials(LoginRequest request) {
        return webClientBuilder.baseUrl(memberServiceBaseUrl)
                .build()
                .post()
                .uri("/auth/login")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(MemberValidationResponse.class);
    }
}

