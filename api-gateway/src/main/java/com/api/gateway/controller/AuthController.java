package com.api.gateway.controller;

import com.api.gateway.dto.request.LoginRequest;
import com.api.gateway.dto.request.RegisterRequest;
import com.api.gateway.dto.response.CustomerLoginResponse;
import com.api.gateway.dto.response.LoginResponse;
import com.api.gateway.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Date;

@RestController
@Slf4j
@RequestMapping("/api-gateway/auth")
@RequiredArgsConstructor
public class AuthController {

    private final WebClient.Builder webClientBuilder;
    private final JwtUtil jwtUtil;
    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${customer.service.url}")
    private String customerServiceUrl;

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<LoginResponse> login(@RequestBody LoginRequest request) {

        return webClientBuilder.build()
                .post()
                .uri(customerServiceUrl + "/internal/auth/login")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(CustomerLoginResponse.class)
                .map(customer -> {

                    String token = jwtUtil.generateToken(
                            customer.getCustomerId(),
                            customer.getEmail()
                    );

                    return LoginResponse.builder()
                            .customerId(customer.getCustomerId())
                            .email(customer.getEmail())
                            .token(token)
                            .build();
                });
    }

    @PostMapping("/logout")
    public Mono<String> logout(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.just("No token found");
        }

        String token = authHeader.substring(7);

        // Try to get expiration claim
        Date exp = jwtUtil.getExpiration(token);

        // If token has no expiration → blacklist for 24 hours by default
        long ttlMs;
        if (exp == null) {
            ttlMs = Duration.ofHours(24).toMillis();  // default TTL
        } else {
            ttlMs = exp.getTime() - System.currentTimeMillis();

            // Prevent negative TTL (expired token)
            if (ttlMs < 0) ttlMs = 0;
        }

        return redisTemplate.opsForValue()
                .set("BLACKLIST:" + token, "1", Duration.ofMillis(ttlMs))
                .then(Mono.just("Logged out successfully"));
    }

    /** -----------------------------------------
     *  Create Customer Endpoint (Gateway)
     * ----------------------------------------- */
    @PostMapping("/register")
    public Mono<ResponseEntity<String>> createNewCustomer(
            @RequestBody RegisterRequest registerRequest
    ) {
        return webClientBuilder.build()
                .post()
                .uri(customerServiceUrl + "/api/customers/createNewCustomer")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(registerRequest)
                .retrieve()
                .bodyToMono(Object.class)
                .map(response -> {
                    try {
                        String json = objectMapper.writerWithDefaultPrettyPrinter()
                                .writeValueAsString(response);
                        return ResponseEntity.ok(json);
                    } catch (Exception e) {
                        return ResponseEntity.internalServerError()
                                .body("Failed to parse response");
                    }
                })
                .onErrorResume(ex -> {
                    log.error("Error creating customer: {}", ex.getMessage());
                    return Mono.just(ResponseEntity
                            .status(500)
                            .body("{\"error\":\"Customer service unreachable\"}"));
                });
    }
}
