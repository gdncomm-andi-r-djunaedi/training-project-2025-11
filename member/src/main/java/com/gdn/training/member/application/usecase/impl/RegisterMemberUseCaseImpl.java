package com.gdn.training.member.application.usecase.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import com.gdn.training.member.application.dto.request.RegisterMemberRequest;
import com.gdn.training.member.application.dto.response.RegisterMemberResponse;
import com.gdn.training.member.application.usecase.RegisterMemberUseCase;
import com.gdn.training.member.domain.exception.MemberAlreadyExistsException;
import com.gdn.training.member.domain.model.Member;
import com.gdn.training.member.domain.password.PasswordHasher;
import com.gdn.training.member.domain.port.out.MemberRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of RegisterMemberUseCase
 * 
 * @author GDN Training
 * @version 1.0
 * @since 2025-12-02
 */
@Slf4j
public class RegisterMemberUseCaseImpl implements RegisterMemberUseCase {

    private final MemberRepository memberRepository;
    private final PasswordHasher passwordHasher;

    public RegisterMemberUseCaseImpl(
            MemberRepository memberRepository,
            PasswordHasher passwordHasher) {
        this.memberRepository = memberRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public RegisterMemberResponse register(RegisterMemberRequest request) {
        log.info("Registering member: {}", request);
        // Check if email exists
        if (memberRepository
                .findByEmail(request.email())
                .isPresent()) {
            throw new MemberAlreadyExistsException("Email already exists");
        }

        // Hash password
        String passwordHash = passwordHasher.hash(request.password());
        // Create domain member
        Member member = new Member(
                UUID.randomUUID(),
                request.fullName(),
                request.email(),
                passwordHash,
                request.phoneNumber(),
                null,
                LocalDateTime.now(),
                LocalDateTime.now());

        // Save member
        memberRepository.save(member);

        log.info("Successfully registered member: {}", member);

        // Return response
        return new RegisterMemberResponse(
                member.getId(),
                member.getFullName(),
                member.getEmail());
    }
}
