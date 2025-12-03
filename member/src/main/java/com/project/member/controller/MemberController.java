package com.project.member.controller;

import com.project.member.dto.AuthResponse;
import com.project.member.dto.LoginRequest;
import com.project.member.dto.RegisterRequest;
import com.project.member.service.AuthService;
import com.project.member.security.JwtService;
import com.project.member.security.RevokedToken;
import com.project.member.repositories.RevokedTokenRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final RevokedTokenRepository revokedTokenRepository;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                // Compute token hash to avoid storing raw JWT
                String tokenHash = sha256Hex(token);
                // Extract token expiration and store blacklist entry
                Instant expiresAt = jwtService.getExpiration(token).toInstant();
                RevokedToken revoked = RevokedToken.builder()
                        .tokenHash(tokenHash)
                        .expiresAt(expiresAt)
                        .createdAt(Instant.now())
                        .build();
                revokedTokenRepository.save(revoked);
            } catch (Exception ignored) {
                // Swallow errors to keep logout idempotent and not leak information
            }
        }
        return ResponseEntity.noContent().build();
    }

    private String sha256Hex(String value) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
}