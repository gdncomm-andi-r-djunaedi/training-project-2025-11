package com.kailash.apigateway.service.impl;

import com.kailash.apigateway.client.MemberClient;
import com.kailash.apigateway.dto.ApiResponse;
import com.kailash.apigateway.dto.LoginRequest;
import com.kailash.apigateway.dto.MemberResponse;
import com.kailash.apigateway.entity.RefreshToken;
import com.kailash.apigateway.exception.NotFoundException;
import com.kailash.apigateway.repository.RefreshTokenRepository;
import com.kailash.apigateway.security.JwtUtil;
//import lombok.Value;
import com.kailash.apigateway.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    RefreshTokenRepository refreshRepo;
    @Autowired
    JwtUtil jwtUtil;
    @Autowired
    MemberClient memberClient;

    @Value("${jwt.expiration-seconds}")
    private long jwtExpirySec;

    @Value("${jwt.refresh-expiration-seconds}")
    private long refreshExpirySec;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Map<String, Object>> login(LoginRequest req) {
        try {
            System.out.println("printing response");
            ResponseEntity<ApiResponse<MemberResponse>> response = memberClient.login(req);
            System.out.println("printing response"+response);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return ApiResponse.failure("Invalid credentials");
            }

            MemberResponse member = response.getBody().getData();

            if (member == null) {
                return ApiResponse.failure("Member data not found");
            }

            String memberId = member.getId();

            Map<String, Object> claims = new HashMap<>();
            claims.put("email", member.getEmail());

            String accessToken = jwtUtil.generateToken(memberId, claims, jwtExpirySec);

            // Save refresh token
            String refreshId = UUID.randomUUID().toString();

            RefreshToken refreshToken = RefreshToken.builder()
                    .jti(refreshId)
                    .memberId(UUID.fromString(memberId))
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(refreshExpirySec))
                    .isRevoked(false)
                    .build();

            refreshRepo.save(refreshToken);

            Map<String, Object> data = new HashMap<>();
            data.put("accessToken", accessToken);
            data.put("expiresIn", jwtExpirySec);
            data.put("refreshTokenId", refreshId);

            return ApiResponse.success(data);

        } catch (Exception ex) {
            return ApiResponse.failure("Login failed: " + ex.getMessage());
        }
    }

    @Override
    public ApiResponse<Map<String, Object>> refresh(String refreshTokenId) {
        try {
            RefreshToken rt = refreshRepo.findByJti(refreshTokenId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

            if (rt.isRevoked() || rt.getExpiresAt().isBefore(Instant.now())) {
                return ApiResponse.failure("Refresh token expired or revoked");
            }

            String memberId = rt.getMemberId().toString();
            String token = jwtUtil.generateToken(memberId, Map.of(), jwtExpirySec);

            Map<String, Object> data = new HashMap<>();
            data.put("accessToken", token);
            data.put("expiresIn", jwtExpirySec);
            data.put("refreshTokenId", refreshTokenId);

            return ApiResponse.success(data);

        } catch (Exception ex) {
            return ApiResponse.failure("Refresh failed: " + ex.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Void> logout(String refreshTokenId) {
        try {
            refreshRepo.findByJti(refreshTokenId).ifPresent(rt -> {
                rt.setRevoked(true);
                refreshRepo.save(rt);
            });

            return ApiResponse.success(null);
        } catch (Exception ex) {
            return ApiResponse.failure("Logout failed: " + ex.getMessage());
        }
    }
}
