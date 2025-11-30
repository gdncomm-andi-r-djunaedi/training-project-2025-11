package com.blibli.training.member.service;

import com.blibli.training.framework.exception.AuthenticationException;
import com.blibli.training.framework.security.JwtUtils;
import com.blibli.training.member.dto.LoginRequest;
import com.blibli.training.member.dto.LoginResponse;
import com.blibli.training.member.dto.RegisterRequest;
import com.blibli.training.member.entity.Member;
import com.blibli.training.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public Member register(RegisterRequest request) {
        if (memberRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        Member member = Member.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .build();

        return memberRepository.save(member);
    }

    public LoginResponse login(LoginRequest request) {
        Member member = memberRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AuthenticationException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new AuthenticationException("Invalid username or password");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("email", member.getEmail());

        String token = jwtUtils.generateToken(String.valueOf(member.getId()), claims);
        return new LoginResponse(token);
    }
}
