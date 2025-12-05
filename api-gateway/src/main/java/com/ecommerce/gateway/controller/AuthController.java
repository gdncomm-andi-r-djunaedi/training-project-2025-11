package com.ecommerce.gateway.controller;

import com.ecommerce.gateway.service.JwtService;
import com.ecommerce.gateway.service.TokenBlacklistService;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/gateway/auth")
@RequiredArgsConstructor
public class AuthController {

        private final RestTemplate restTemplate;
        private final JwtService jwtService;
        private final TokenBlacklistService tokenBlacklistService;

        @PostMapping("/login")
        public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {

                // Forward to Member Service
                Map member = restTemplate.postForObject(
                                "http://localhost:8081/api/members/login",
                                loginRequest,
                                Map.class);

                Long userId = Long.valueOf(member.get("id").toString());
                String username = member.get("username").toString();

                String token = jwtService.generateToken(username, userId);

                // Return token in response body
                Map<String, Object> response = new java.util.HashMap<>();
                response.put("token", token);
                response.put("userId", userId);
                response.put("username", username);

                return ResponseEntity.ok(response);
        }

        @PostMapping("/logout")
        public ResponseEntity<Map<String, String>> logout(
                        @RequestHeader(value = "Authorization", required = false) String authHeader) {

                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);
                        // Blacklist token with 1 hour TTL (matching JWT expiration)
                        tokenBlacklistService.blacklistToken(token, 3600).block();
                }

                return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
        }
}
