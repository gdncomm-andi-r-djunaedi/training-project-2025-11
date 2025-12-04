package com.gdn.training.apigateway.infrastructure.controller;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gdn.training.apigateway.application.dto.LoginRequest;
import com.gdn.training.apigateway.application.dto.LogoutResponse;
import com.gdn.training.apigateway.application.usecase.LoginMemberUseCase;
import com.gdn.training.apigateway.application.usecase.LogoutMemberUseCase;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginMemberUseCase loginUseCase;
    private final LogoutMemberUseCase logoutUseCase;

    @Operation(summary = "Login")
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

    @Operation(summary = "Logout")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");

        logoutUseCase.logout(token);

        return ResponseEntity.ok(new LogoutResponse("Logged out"));
    }
}
