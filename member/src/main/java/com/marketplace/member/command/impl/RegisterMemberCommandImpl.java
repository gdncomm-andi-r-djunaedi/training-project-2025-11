package com.marketplace.member.command.impl;

import com.marketplace.common.aspect.Auditable;
import com.marketplace.member.command.RegisterMemberCommand;
import com.marketplace.member.dto.request.RegisterRequest;
import com.marketplace.member.dto.response.MemberResponse;
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
    @Auditable(action = "USER_REGISTER", description = "New user registration")
    public MemberResponse execute(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        if (memberRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed - email already exists: {}", request.getEmail());
            throw UserAlreadyExistsException.email(request.getEmail());
        }

        if (request.getPhoneNumber() != null && memberRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            log.warn("Registration failed - phone number already exists: {}", request.getPhoneNumber());
            throw UserAlreadyExistsException.phoneNumber(request.getPhoneNumber());
        }

        Member member = Member.builder()
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .fullName(request.getFullName())
                .address(request.getAddress())
                .phoneNumber(request.getPhoneNumber())
                .build();

        Member savedMember = memberRepository.save(member);
        log.info("User registered successfully: {}", savedMember.getEmail());

        return MemberResponse.builder()
                .id(savedMember.getId())
                .email(savedMember.getEmail())
                .fullName(savedMember.getFullName())
                .address(savedMember.getAddress())
                .phoneNumber(savedMember.getPhoneNumber())
                .build();
    }
}
