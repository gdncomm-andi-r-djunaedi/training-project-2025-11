package com.marketplace.member.service;

import com.marketplace.common.dto.UserDetailsResponse;
import com.marketplace.common.dto.ValidateCredentialsRequest;
import com.marketplace.member.dto.MemberResponse;
import com.marketplace.member.dto.RegisterRequest;
import com.marketplace.member.entity.Member;
import com.marketplace.member.exception.InvalidCredentialsException;
import com.marketplace.member.exception.UserAlreadyExistsException;
import com.marketplace.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public MemberResponse register(RegisterRequest request) {
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

    /**
     * Validate user credentials (called by API Gateway)
     * Returns user details without generating JWT
     */
    public UserDetailsResponse validateCredentials(ValidateCredentialsRequest request) {
        log.info("Validating credentials for user: {}", request.getUsername());

        Member member = memberRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    log.warn("Credential validation failed - user not found: {}", request.getUsername());
                    return new InvalidCredentialsException();
                });

        if (!passwordEncoder.matches(request.getPassword(), member.getPasswordHash())) {
            log.warn("Credential validation failed - invalid password for user: {}", request.getUsername());
            throw new InvalidCredentialsException();
        }

        log.info("Credentials validated successfully for user: {}", member.getUsername());

        // Return user details (Gateway will create JWT)
        return UserDetailsResponse.builder()
                .id(member.getId())
                .username(member.getUsername())
                .email(member.getEmail())
                .fullName(member.getFullName())
                .roles(new java.util.ArrayList<>(member.getRoles())) // Convert Set to List
                .build();
    }
}
