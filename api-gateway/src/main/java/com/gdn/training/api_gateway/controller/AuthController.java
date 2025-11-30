package com.gdn.training.api_gateway.controller;

import com.gdn.training.api_gateway.client.MemberClient;
import com.gdn.training.api_gateway.dto.LoginRequest;
import com.gdn.training.api_gateway.dto.LoginResponse;
import com.gdn.training.api_gateway.dto.UserInfoDTO;
import com.gdn.training.api_gateway.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication endpoints (handled by Gateway)")
public class AuthController {

    private static final String ACCESS_TOKEN_COOKIE = "ACCESS_TOKEN";
    private static final String COOKIE_PATH_ROOT = "/";
    private static final String SAME_SITE_STRICT = "Strict";

    private final MemberClient memberClient;
    private final JwtService jwtService;

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Login and receive JWT token in secure cookie")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        UserInfoDTO userInfo = memberClient.validateCredentials(request);

        String token = jwtService.generateToken(
                userInfo.getId().toString(),
                Map.of(
                        "email", userInfo.getEmail(),
                        "role", userInfo.getRole()
                )
        );

        ResponseCookie cookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE, token)
                .httpOnly(true)
                .secure(true)
                .sameSite(SAME_SITE_STRICT)
                .path(COOKIE_PATH_ROOT)
                .maxAge(jwtService.getExpirationSeconds())
                .build();

        LoginResponse response = new LoginResponse(token);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Logout and clear authentication cookie")
    public ResponseEntity<Void> logout() {
        ResponseCookie cookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(true)
                .sameSite(SAME_SITE_STRICT)
                .path(COOKIE_PATH_ROOT)
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }
}
