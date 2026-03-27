package com.marketplace.member.service.impl;

import com.marketplace.member.dto.LoginRequest;
import com.marketplace.member.dto.MemberResponse;
import com.marketplace.member.dto.RegisterRequest;
import com.marketplace.member.entity.Member;
import com.marketplace.member.exception.EmailAlreadyExistsException;
import com.marketplace.member.exception.InvalidCredentialsException;
import com.marketplace.member.repository.MemberRepository;
import com.marketplace.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public MemberResponse register(RegisterRequest request) {
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());
        
        Member member = Member.builder()
                .email(request.getEmail())
                .passwordHash(hashedPassword)
                .name(request.getName())
                .build();

        Member savedMember = memberRepository.save(member);
        log.info("User registered successfully with email: {}", request.getEmail());

        MemberResponse response = new MemberResponse();
        BeanUtils.copyProperties(savedMember, response);
        return response;
    }

    @Override
    public MemberResponse login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.getPassword(), member.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        log.info("User logged in successfully with email: {}", request.getEmail());

        MemberResponse response = new MemberResponse();
        BeanUtils.copyProperties(member, response);
        return response;
    }
}
