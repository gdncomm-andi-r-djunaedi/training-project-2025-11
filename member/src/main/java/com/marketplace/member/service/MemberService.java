package com.marketplace.member.service;

import com.marketplace.member.dto.AuthResponse;
import com.marketplace.member.dto.LoginRequest;
import com.marketplace.member.dto.MemberDto;
import com.marketplace.member.dto.RegisterRequest;
import com.marketplace.member.entity.Member;
import com.marketplace.member.exception.AuthenticationException;
import com.marketplace.member.exception.DuplicateResourceException;
import com.marketplace.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new member with email: {}", request.getEmail());

        // Check if email already exists
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered");
        }

        // Check if username already exists
        if (memberRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already taken");
        }

        // Hash password using BCrypt
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // Create member entity
        Member member = Member.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .passwordHash(hashedPassword)
                .fullName(request.getFullName())
                .build();

        // Save to database
        Member savedMember = memberRepository.save(member);
        log.info("Member registered successfully with ID: {}", savedMember.getId());

        // Generate JWT token
        String token = jwtService.generateToken(
                savedMember.getId(),
                savedMember.getEmail(),
                savedMember.getUsername());

        // Return auth response
        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .expiresIn(jwtService.getExpirationMs())
                .member(convertToDto(savedMember))
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for: {}", request.getEmailOrUsername());

        // Find member by email or username
        Member member = memberRepository.findByEmail(request.getEmailOrUsername())
                .or(() -> memberRepository.findByUsername(request.getEmailOrUsername()))
                .orElseThrow(() -> new AuthenticationException("Invalid credentials"));

        // Verify password using BCrypt
        if (!passwordEncoder.matches(request.getPassword(), member.getPasswordHash())) {
            throw new AuthenticationException("Invalid credentials");
        }

        log.info("Login successful for member ID: {}", member.getId());

        // Generate JWT token
        String token = jwtService.generateToken(
                member.getId(),
                member.getEmail(),
                member.getUsername());

        // Return auth response
        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .expiresIn(jwtService.getExpirationMs())
                .member(convertToDto(member))
                .build();
    }

    public void logout(String token) {
        log.info("Logout request received");

        // Add token to blacklist
        tokenBlacklistService.blacklistToken(token);

        log.info("Logout successful, token blacklisted");
    }

    private MemberDto convertToDto(Member member) {
        return MemberDto.builder()
                .id(member.getId())
                .email(member.getEmail())
                .username(member.getUsername())
                .fullName(member.getFullName())
                .createdAt(member.getCreatedAt())
                .build();
    }
}
