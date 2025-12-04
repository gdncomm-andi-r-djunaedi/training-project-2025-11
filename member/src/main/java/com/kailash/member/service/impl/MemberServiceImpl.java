package com.kailash.member.service.impl;

import com.auth0.jwt.algorithms.Algorithm;
import com.kailash.member.dto.*;
import com.kailash.member.entity.Member;
import com.kailash.member.entity.RefreshToken;
import com.kailash.member.exception.NotFoundException;
import com.kailash.member.repository.MemberRepository;
import com.kailash.member.repository.RefreshTokenRepository;
import com.kailash.member.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import com.auth0.jwt.JWT;

@Service
public class MemberServiceImpl implements MemberService {
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    RefreshTokenRepository refreshTokenRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Value("${jwt.expiration-seconds:1800}")
    private long jwtExpiry;
    @Value("${jwt.secret}") private String jwtSecret;
    @Value("${jwt.expiration-seconds}") private long jwtExpirySec;
    @Value("${jwt.refresh-expiration-seconds}") private long refreshExpirySec;

    @Override
    public ApiResponse<MemberResponse> register(RegisterRequest req) {
        memberRepository.findByEmail(req.getEmail()).ifPresent(member -> {
            throw new IllegalArgumentException("Email is already registered");
        });

        Member member = Member.builder()
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .fullName(req.getFullName())
                .phone(req.getPhone())
                .createdAt(Instant.now())
                .build();

        Member saved = memberRepository.save(member);

        return new ApiResponse<>(
                toResponse(saved),
                true,
                "Member registered successfully"
        );
    }

    @Override
    public ApiResponse<MemberResponse> login(LoginRequest req) {
        Member member = memberRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new NotFoundException("Invalid credentials"));

        if (!passwordEncoder.matches(req.getPassword(), member.getPasswordHash())) {
            throw new NotFoundException("Invalid credentials");
        }

        return new ApiResponse<>(toResponse(member), true, "Login successful");
    }

    @Override
    public ApiResponse<MemberResponse> getById(UUID id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Member not present with given id"));

        return new ApiResponse<>(toResponse(member), true, "Member retrieved successfully");
    }

    @Override
    public ApiResponse<MemberResponse> update(UUID id, RegisterRequest req) {
        Member m = memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Member not found"));
        if (req.getFullName() != null) m.setFullName(req.getFullName());
        if (req.getPhone() != null) m.setPhone(req.getPhone());
        if (req.getPassword()!=null)m.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        Member saved = memberRepository.save(m);

        return new ApiResponse<>(toResponse(saved), true, "Member updated successfully");
    }

    @Override
    public ApiResponse<Void> delete(UUID id) {
        memberRepository.deleteById(id);
        return new ApiResponse<>(null, true, "Member deleted successfully");
    }

    // helper method
    private MemberResponse toResponse(Member member) {
        return new MemberResponse(member.getId().toString(), member.getEmail(), member.getFullName(), member.getPhone());
    }

}
