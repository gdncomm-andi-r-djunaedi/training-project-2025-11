package com.example.apigateway.controller;

import com.example.apigateway.client.MemberClient;
import com.example.apigateway.dto.LoginRequest;
import com.example.apigateway.dto.LoginResponse;
import com.example.apigateway.dto.MemberValidationResponse;
import com.example.apigateway.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MemberClient memberClient;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public Mono<ResponseEntity<LoginResponse>> login(@RequestBody Mono<LoginRequest> loginRequest,
            ServerHttpResponse response) {
        return loginRequest
                .flatMap(memberClient::validateCredentials)
                .map(member -> buildSuccessfulLoginResponse(member, response));
    }

    @PostMapping("/logout")
    public Mono<ResponseEntity<String>> logout(ServerHttpResponse response) {
        ResponseCookie expiredCookie = jwtUtil.clearCookie();
        response.addCookie(expiredCookie);
        return Mono.just(ResponseEntity.ok("Logged out"));
    }

    private ResponseEntity<LoginResponse> buildSuccessfulLoginResponse(MemberValidationResponse member,
            ServerHttpResponse response) {
        String token = jwtUtil.createToken(String.valueOf(member.getUserId()));
        ResponseCookie cookie = jwtUtil.createCookie(token);
        response.addCookie(cookie);
        return ResponseEntity.ok(new LoginResponse(token));
    }
}

