package com.blibi.member.serviceImpl;

import com.blibi.member.dto.MemberLoginRequest;
import com.blibi.member.dto.MemberRegisterRequest;
import com.blibi.member.dto.MemberResponse;
import com.blibi.member.entity.Member;
import com.blibi.member.exception.MemberNotFoundException;
import com.blibi.member.repository.MemberRepository;
import com.blibi.member.service.MemberService;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Builder
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberServiceImpl(MemberRepository memberRepository,
            PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public MemberResponse register(MemberRegisterRequest request) {
        log.info("Registering new member: {}", request.getUserName());
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        Member member = Member.builder()
                .userName(request.getUserName())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .isActive(true)
                .build();
        memberRepository.save(member);

        return MemberResponse.builder()
                .userId(member.getId())
                .userName(member.getUserName())
                .email(member.getEmail())
                .active(true)
                .build();
    }

    @Override
    public MemberResponse login(MemberLoginRequest request) {
        log.info("Login Request:{}", request.getUserName());
        Member member = memberRepository.findByUserName(request.getUserName())
                .orElseThrow(() -> new MemberNotFoundException("User not found"));
        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new RuntimeException("Wrong Creds");
        }
        return MemberResponse.builder()
                .userId(member.getId())
                .userName(member.getUserName())
                .email(member.getEmail())
                .active(true)
                .build();
    }
}
