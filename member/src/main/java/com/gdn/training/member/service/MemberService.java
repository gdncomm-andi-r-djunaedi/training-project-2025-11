package com.gdn.training.member.service;

import com.gdn.training.member.dto.AuthResponse;
import com.gdn.training.member.dto.LoginRequest;
import com.gdn.training.member.dto.RegisterRequest;
import com.gdn.training.member.entity.Member;
import com.gdn.training.member.repository.MemberRepository;
import com.gdn.training.member.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisService redisService;

    public String register(RegisterRequest request) {
        if (memberRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        Member member = Member.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .build();

        memberRepository.save(member);
        return "User registered successfully";
    }

    public AuthResponse login(LoginRequest request) {
        Member member = memberRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(member.getUsername(), member.getId());
        return AuthResponse.builder()
                .token(token)
                .memberId(member.getId())
                .username(member.getUsername())
                .build();
    }

    public void logout(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        // Default expiration 30 mins
        redisService.blacklistToken(token, 1000 * 60 * 30);
    }
}
