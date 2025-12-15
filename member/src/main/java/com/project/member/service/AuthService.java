package com.project.member.service;

import com.project.member.dto.AuthResponse;
import com.project.member.dto.LoginRequest;
import com.project.member.dto.RegisterRequest;
import com.project.member.entity.Member;
import com.project.member.repositories.MemberRepository;
import com.project.member.repositories.RevokedTokenRepository;
import com.project.member.security.JwtService;
import com.project.member.security.RevokedToken;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final MemberRepository memberRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RevokedTokenRepository revokedTokenRepository;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        Optional<Member> existing = memberRepository.findByEmail(request.getEmail());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }
        Member member = Member.builder()
                .email(request.getEmail())
                .fullName(request.getFullName())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .status("ACTIVE")
                .createdBy("system")
                .build();
        memberRepository.save(member);
        String token = jwtService.generateToken(member.getEmail());
        return new AuthResponse(token);
    }

    public AuthResponse login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (!passwordEncoder.matches(request.getPassword(), member.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        String token = jwtService.generateToken(member.getEmail());
        return new AuthResponse(token);
    }

    /**
     * Logout user by revoking the JWT token
     * @param token JWT token to revoke
     */
    @Transactional
    public void logout(String token) {
        if (token == null || token.isEmpty()) {
            return; // Idempotent - no token to revoke
        }
        
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
        } catch (Exception e) {
            // Swallow errors to keep logout idempotent
            // and not leak information about token validity
        }
    }

    /**
     * Compute SHA-256 hash of a string
     */
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