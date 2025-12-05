package com.gdn.faurihakim.gateway.service;

import com.gdn.faurihakim.gateway.model.AuthRequest;
import com.gdn.faurihakim.gateway.model.VerifyCredentialsResponse;
import com.gdn.faurihakim.gateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class AuthService {

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${member.service.url}")
    private String memberServiceUrl;

    public Mono<String> login(AuthRequest request) {

        return webClientBuilder.build()
                .post()
                .uri(memberServiceUrl + "/api/members/verify")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(VerifyCredentialsResponse.class)
                .flatMap(response -> {

                    // If verification failed or no data → return empty
                    if (response.getData() == null || response.getData().getMemberId() == null) {
                        return Mono.empty();
                    }

                    // Credentials valid → generate token
                    String memberId = response.getData().getMemberId();
                    return Mono.just(jwtUtil.generateToken(memberId));
                })
                .onErrorResume(e -> Mono.empty());
    }
}
