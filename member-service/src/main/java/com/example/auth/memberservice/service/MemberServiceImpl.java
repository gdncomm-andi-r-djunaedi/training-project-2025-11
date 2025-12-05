package com.example.auth.memberservice.service;

import com.example.auth.memberservice.dto.LoginRequest;
import com.example.auth.memberservice.dto.LoginResponse;
import com.example.auth.memberservice.dto.MemberRegisterRequest;
import com.example.auth.memberservice.dto.MemberRegisterResponse;
import com.example.auth.memberservice.exception.BusinessException;
import com.example.auth.memberservice.constant.Role;
import com.example.auth.memberservice.model.Member;
import com.example.auth.memberservice.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public MemberRegisterResponse register(MemberRegisterRequest request) {
        String email = request.email().toLowerCase();

        if (memberRepository.existsByEmail(email)) {
            throw new BusinessException("EMAIL_ALREADY_USED", "Email already registered");
        }

        Member member = Member.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(Role.CUSTOMER.name())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Member saved = memberRepository.save(member);

        return new MemberRegisterResponse(saved.getId(), saved.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MemberRegisterResponse> getAllUsers(Pageable pageable) {
        return memberRepository.findAll(pageable)
                .map(member -> new MemberRegisterResponse(member.getId(), member.getEmail()));
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        String email = request.email().toLowerCase();

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("INVALID_CREDENTIALS", "Email not exist"));

        if (!passwordEncoder.matches(request.password(), member.getPasswordHash())) {
            throw new BusinessException("INVALID_CREDENTIALS", "Invalid email or password");
        }

        member.setUpdatedAt(Instant.now());
        memberRepository.save(member);

        return LoginResponse.fromMember(member);
    }
}