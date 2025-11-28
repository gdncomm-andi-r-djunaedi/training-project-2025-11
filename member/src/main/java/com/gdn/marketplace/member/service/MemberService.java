package com.gdn.marketplace.member.service;

import com.gdn.marketplace.member.dto.AuthResponse;
import com.gdn.marketplace.member.dto.LoginRequest;
import com.gdn.marketplace.member.dto.RegisterRequest;
import com.gdn.marketplace.member.entity.Member;
import com.gdn.marketplace.member.repository.MemberRepository;
import com.gdn.marketplace.member.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public Member register(RegisterRequest request) {
        if (memberRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        Member member = new Member();
        member.setUsername(request.getUsername());
        member.setEmail(request.getEmail());
        member.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        return memberRepository.save(member);
    }

    public AuthResponse login(LoginRequest request) {
        Member member = memberRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), member.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(member.getUsername());
        return new AuthResponse(token);
    }

    @Autowired
    private org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

    public void logout(String token) {
        // Token is "Bearer <token>" or just "<token>"
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        // Blacklist token with expiration (e.g. 1 hour, matching JWT TTL)
        redisTemplate.opsForValue().set("blacklist:" + token, "true", java.time.Duration.ofHours(1));
    }

    public Member getProfile(String username) {
        return memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
