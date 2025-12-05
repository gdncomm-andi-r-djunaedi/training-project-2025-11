package com.gdn.faurihakim.gateway.service;

import com.gdn.faurihakim.gateway.model.AuthRequest;
import com.gdn.faurihakim.gateway.model.VerifyCredentialsResponse;
import com.gdn.faurihakim.gateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class AuthService {

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Autowired
    private JwtUtil jwtUtil;

    /*public Mono<String> login(AuthRequest request) {
        return webClientBuilder.build()
                .post()
                .uri("http://localhost:8081/api/members/verify")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(VerifyCredentialsResponse.class) // Need to define this wrapper or use JsonNode
                .map(response -> {
                    if (response.getData() != null) {
                        return jwtUtil.generateToken(response.getData().getMemberId());
                    }
                    return null;
                });
    }*/

    public Mono<String> login(AuthRequest request) {

        return webClientBuilder.build()
                .post()
                .uri("http://localhost:8081/api/members/verify")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(VerifyCredentialsResponse.class)
                .flatMap(response -> {

                    // If verification failed → return empty
                    if (!response.isSuccess() || response.getData() == null) {
                        return Mono.empty();
                    }

                    // Credentials valid → generate token
                    String memberId = response.getData().getMemberId();
                    return Mono.just(jwtUtil.generateToken(memberId));
                })
                .onErrorResume(e -> Mono.empty()); // Optional: hide external errors
    }
}
