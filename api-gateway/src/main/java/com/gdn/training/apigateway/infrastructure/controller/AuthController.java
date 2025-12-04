package com.gdn.training.apigateway.infrastructure.controller;

import com.gdn.training.apigateway.application.dto.*;
import com.gdn.training.apigateway.application.usecase.LoginMemberUseCase;
import com.gdn.training.apigateway.application.usecase.LogoutMemberUseCase;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginMemberUseCase loginUseCase;
    private final LogoutMemberUseCase logoutUseCase;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req, HttpServletResponse response) {

        String token = loginUseCase.login(req.email(), req.password());

        // HttpOnly cookie
        ResponseCookie cookie = ResponseCookie.from("jwt", token)
                .path("/")
                .httpOnly(true)
                .maxAge(3600)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + token)
                .body(Map.of("token", token));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");

        logoutUseCase.logout(token);

        return ResponseEntity.ok(new LogoutResponse("Logged out"));
    }
}
