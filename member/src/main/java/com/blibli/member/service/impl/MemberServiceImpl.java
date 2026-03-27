package com.blibli.member.service.impl;

import com.blibli.member.dto.LoginRequest;
import com.blibli.member.dto.MemberResponse;
import com.blibli.member.dto.RegisterRequest;
import com.blibli.member.entity.Member;
import com.blibli.member.entity.Role;
import com.blibli.member.exception.BadRequestException;
import com.blibli.member.exception.ResourceNotFoundException;
import com.blibli.member.exception.UnauthorizedException;
import com.blibli.member.repository.MemberRepository;
import com.blibli.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public MemberResponse register(RegisterRequest request)  {
        log.info("Registering new member with email: {}", request.getEmail());

        if (memberRepository.existsByEmail(request.getEmail().trim())) {
            throw new BadRequestException("Email already registered");
        }
        if(memberRepository.existsByEmailIgnoreCase(request.getEmail().trim())){
            throw new BadRequestException("Email already registered");
        }

        Member member = Member.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .roles(Set.of(Role.CUSTOMER))
                .isActive(true)
                .build();

        Member savedMember = memberRepository.save(member);
        log.info("Member registered successfully with ID: {}", savedMember.getId());

        return toMemberResponse(savedMember);
    }

    @Override
    public MemberResponse authenticate(LoginRequest request) {
        log.info("Authenticating member with email: {}", request.getEmail());

        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!member.getIsActive()) {
            throw new UnauthorizedException("Account is inactive");
        }

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        log.info("Member authenticated successfully: {}", member.getId());
        
        // Return MemberResponse - token generation is handled by the gateway
        return toMemberResponse(member);
    }

    @Override
    public MemberResponse getMemberById(UUID id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
        return toMemberResponse(member);
    }

    @Override
    public MemberResponse getMemberByEmail(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
        return toMemberResponse(member);

    }

    private MemberResponse toMemberResponse(Member member) {
        return MemberResponse.builder()
                .id(member.getId().toString())
                .email(member.getEmail())
                .firstName(member.getFirstName())
                .lastName(member.getLastName())
                .roles(member.getRoles().stream()
                        .map(Enum::name)
                        .collect(Collectors.toSet()))
                .createdAt(member.getCreatedAt())
                .build();
    }
}
