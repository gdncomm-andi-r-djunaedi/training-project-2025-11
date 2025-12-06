package com.gdn.training.member.application.usecase.impl;

import com.gdn.training.member.application.dto.request.LoginMemberRequest;
import com.gdn.training.member.application.dto.response.LoginMemberResponse;
import com.gdn.training.member.application.usecase.LoginMemberUseCase;
import com.gdn.training.member.domain.password.PasswordHasher;
import com.gdn.training.member.domain.port.out.MemberRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of LoginMemberUseCase
 * 
 * @author GDN Training
 * @version 1.0
 * @since 2025-12-02
 */
@Slf4j
public class LoginMemberUseCaseImpl implements LoginMemberUseCase {
    private final MemberRepository memberRepository;
    private final PasswordHasher passwordHasher;

    public LoginMemberUseCaseImpl(MemberRepository memberRepository, PasswordHasher passwordHasher) {
        this.memberRepository = memberRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public LoginMemberResponse login(LoginMemberRequest request) {
        log.info("Logging in member: {}", request);
        // Check if email exists
        if (memberRepository.findByEmail(request.email()).isEmpty()) {
            return new LoginMemberResponse(false, null, null, null);
        }

        var member = memberRepository.findByEmail(request.email()).get();
        boolean matches = passwordHasher.matches(request.rawPassword(), member.getPasswordHash());

        if (!matches) {
            return new LoginMemberResponse(false, null, null, null);
        }

        return new LoginMemberResponse(true, member.getId(), member.getFullName(), member.getEmail());

    }
}
