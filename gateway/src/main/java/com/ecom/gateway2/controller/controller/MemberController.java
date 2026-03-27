package com.ecom.gateway2.controller.controller;

import com.ecom.gateway2.controller.Dto.ApiResponse;
import com.ecom.gateway2.controller.controller.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.Authenticator;
import java.time.Duration;
import java.util.Map;
import java.util.Set;

import static com.ecom.gateway2.controller.Config.AppConfig.TOKEN_EXPIRATION;

@Slf4j
@RestController
@RequestMapping("/member")
public class MemberController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String, String> loginRequest) {

        ApiResponse response = webClientBuilder.build()
                .post()
                .uri("http://localhost:8504/member/login")
                .bodyValue(loginRequest)
                .retrieve()
                .bodyToMono(ApiResponse.class)
                .block();

        if (response.getCode() != 200) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid credentials");
        }

        String userId = response.getData().toString();
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("UserId is required");
        }

        String redisKey = userId;
        String existingToken = redisTemplate.opsForValue().get(userId);

        if (existingToken != null && !existingToken.isEmpty()) {
            try {
                jwtUtil.validateToken(existingToken);
                log.info("Returning existing token for user: {}", userId);
                return ResponseEntity.ok()
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + existingToken)
                        .body("Login successful. Using existing session. User ID: " + userId);
            } catch (Exception e) {
                log.warn("Existing token invalid for user: {}, generating new token", userId);
                redisTemplate.delete(redisKey);
            }
        }

        String token = jwtUtil.generateToken(userId);
        redisTemplate.opsForValue().set(redisKey, token, Duration.ofMillis(TOKEN_EXPIRATION));

        log.info("TOKEN GENERATED : {}", token);

        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .body("Login successful. User ID: " + userId);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody String userId) {

        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Username is required");
        }

        userId = userId.trim().replace("\"", "");
        Boolean deleted = redisTemplate.delete(userId);

        if (deleted.equals(true)) {
            return ResponseEntity.ok()
                    .body("Logout success for user: " + userId);
        } else {
            return ResponseEntity.ok()
                    .body("No active session for: " + userId);
        }
    }
}

