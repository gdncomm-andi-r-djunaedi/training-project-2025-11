package com.blibi.apigateway.controller;

import com.blibi.apigateway.dto.GenericResponse;
import com.blibi.apigateway.dto.LoginRequest;
import com.blibi.apigateway.dto.LoginResponse;
import com.blibi.apigateway.service.AuthService;
import com.blibi.apigateway.serviceImpl.AuthServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")

    public GenericResponse<LoginResponse> login(@RequestBody LoginRequest request) {

        /** Requirement: Login with JWT response */

        return GenericResponse.<LoginResponse>builder()
                .status("SUCCESS")
                .message("Login successful")
                .data(authService.login(request))
                .build();
    }

    @PostMapping("/logout")
    public GenericResponse<String> logout(@RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        authService.logout(token);

        return GenericResponse.<String>builder()
                .status("SUCCESS")
                .message("Logged out and token invalidated")
                .data("OK")
                .build();
    }

}
