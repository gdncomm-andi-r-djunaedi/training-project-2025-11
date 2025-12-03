package com.gdn.training.api_gateway.controller;

import java.util.Map;

import io.jsonwebtoken.Claims;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gdn.training.api_gateway.client.MemberClient;
import com.gdn.training.api_gateway.dto.LoginRequest;
import com.gdn.training.api_gateway.dto.LoginResponse;
import com.gdn.training.api_gateway.dto.RegisterRequest;
import com.gdn.training.api_gateway.dto.UserInfoDTO;
import com.gdn.training.api_gateway.security.AccessTokenResolver;
import com.gdn.training.api_gateway.security.JwtService;
import com.gdn.training.api_gateway.security.TokenBlacklistService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private static final String ACCESS_TOKEN_COOKIE = "ACCESS_TOKEN";
    private static final String COOKIE_PATH_ROOT = "/";
    private static final String SAME_SITE_STRICT = "Strict";

    private final MemberClient memberClient;
    private final JwtService jwtService;
    private final AccessTokenResolver accessTokenResolver;
    private final TokenBlacklistService tokenBlacklistService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Register attempt started for {}", request.getEmail());
        memberClient.register(request);
        log.info("Register succeeded for {}", request.getEmail());
        return ResponseEntity.ok(Map.of("message", "User registered successfully"));
    }


    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt started for {}", request.getEmail());
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

        log.info("Login succeeded for {}", userInfo.getEmail());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String token = accessTokenResolver.resolve(request);
        if (StringUtils.hasText(token)) {
            try {
                Claims claims = jwtService.parseToken(token);
                tokenBlacklistService.blacklist(claims.getId(), jwtService.remainingTtl(claims));
                log.debug("Token {} blacklisted until {}", claims.getId(), claims.getExpiration());
            } catch (Exception ex) {
                log.warn("Failed to blacklist token during logout: {}", ex.getMessage());
            }
        }

        ResponseCookie cookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(true)
                .sameSite(SAME_SITE_STRICT)
                .path(COOKIE_PATH_ROOT)
                .maxAge(0)
                .build();

        log.info("Logout executed, authentication cookie cleared");
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

}
