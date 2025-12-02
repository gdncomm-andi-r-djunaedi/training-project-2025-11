package com.gdn.training.member.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gdn.training.member.dto.LoginRequest;
import com.gdn.training.member.dto.RegisterRequest;
import com.gdn.training.member.dto.UserInfoResponse;
import com.gdn.training.member.entity.Member;
import com.gdn.training.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private static final String DEFAULT_ROLE_USER = "ROLE_USER";

    @Transactional
    public void register(RegisterRequest request) {
        if (memberRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration rejected, email already registered: {}", request.getEmail());
            throw new IllegalArgumentException("Email already registered");
        }

        Member member = Member.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(DEFAULT_ROLE_USER)
                .build();

        Member saved = memberRepository.save(member);
        log.info("New member registered with id {} and email {}", saved.getId(), saved.getEmail());
    }

    @Transactional(readOnly = true)
    public UserInfoResponse validateCredentials(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            log.warn("Invalid password attempt for {}", request.getEmail());
            throw new IllegalArgumentException("Invalid credentials");
        }

        UserInfoResponse response = UserInfoResponse.builder()
                .id(member.getId())
                .email(member.getEmail())
                .name(member.getName())
                .role(member.getRole())
                .build();

        log.debug("Password validated for {}", member.getEmail());
        return response;
    }
}