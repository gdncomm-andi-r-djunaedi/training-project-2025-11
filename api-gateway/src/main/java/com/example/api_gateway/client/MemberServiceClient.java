package com.example.api_gateway.client;

import com.example.api_gateway.config.FeignConfig;
import com.example.api_gateway.response.AuthResponse;
import com.example.api_gateway.request.LoginRequest;
import com.example.api_gateway.response.MessageResponse;
import com.example.api_gateway.request.RegisterRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "member", url = "${services.member.url}", configuration = FeignConfig.class)
public interface MemberServiceClient {

    @PostMapping("/api/users/register")
    ResponseEntity<MessageResponse> register(@RequestBody RegisterRequest request);

    @PostMapping("/api/users/login")
    ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request);
}

