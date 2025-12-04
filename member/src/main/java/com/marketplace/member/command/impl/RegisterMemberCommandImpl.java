package com.marketplace.member.command.impl;

import com.marketplace.member.command.RegisterMemberCommand;
import com.marketplace.member.dto.MemberResponse;
import com.marketplace.member.dto.RegisterRequest;
import com.marketplace.member.entity.Member;
import com.marketplace.member.exception.UserAlreadyExistsException;
import com.marketplace.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterMemberCommandImpl implements RegisterMemberCommand {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public MemberResponse execute(RegisterRequest request) {
        log.info("Registering new user: {}", request.getUsername());

        if (memberRepository.existsByUsername(request.getUsername())) {
            log.warn("Registration failed - username already exists: {}", request.getUsername());
            throw UserAlreadyExistsException.username(request.getUsername());
        }
        if (memberRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed - email already exists: {}", request.getEmail());
            throw UserAlreadyExistsException.email(request.getEmail());
        }

        Member member = Member.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .fullName(request.getFullName())
                .address(request.getAddress())
                .phoneNumber(request.getPhoneNumber())
                .build();

        Member savedMember = memberRepository.save(member);
        log.info("User registered successfully: {}", savedMember.getUsername());

        return MemberResponse.builder()
                .id(savedMember.getId())
                .username(savedMember.getUsername())
                .email(savedMember.getEmail())
                .fullName(savedMember.getFullName())
                .address(savedMember.getAddress())
                .phoneNumber(savedMember.getPhoneNumber())
                .build();
    }
}

