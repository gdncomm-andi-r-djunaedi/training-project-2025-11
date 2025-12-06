package com.marketplace.member.command.impl;

import com.marketplace.common.dto.UserDetailsResponse;
import com.marketplace.common.dto.ValidateCredentialsRequest;
import com.marketplace.member.command.ValidateCredentialsCommand;
import com.marketplace.member.entity.Member;
import com.marketplace.member.exception.InvalidCredentialsException;
import com.marketplace.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Slf4j
@Component
@RequiredArgsConstructor
public class ValidateCredentialsCommandImpl implements ValidateCredentialsCommand {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetailsResponse execute(ValidateCredentialsRequest request) {
        log.info("Validating credentials for email: {}", request.getEmail());

        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Credential validation failed - user not found: {}", request.getEmail());
                    return new InvalidCredentialsException();
                });

        if (!passwordEncoder.matches(request.getPassword(), member.getPasswordHash())) {
            log.warn("Credential validation failed - invalid password for email: {}", request.getEmail());
            throw new InvalidCredentialsException();
        }

        log.info("Credentials validated successfully for email: {}", member.getEmail());

        return UserDetailsResponse.builder()
                .id(member.getId())
                .email(member.getEmail())
                .fullName(member.getFullName())
                .roles(new ArrayList<>(member.getRoles()))
                .build();
    }
}
