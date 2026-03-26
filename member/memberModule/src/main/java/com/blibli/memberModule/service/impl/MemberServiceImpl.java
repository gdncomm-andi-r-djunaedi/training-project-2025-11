package com.blibli.memberModule.service.impl;

import com.blibli.memberModule.dto.LoginRequestDto;
import com.blibli.memberModule.dto.LoginResponseDto;
import com.blibli.memberModule.dto.MemberRequestDto;
import com.blibli.memberModule.dto.MemberResponseDto;
import com.blibli.memberModule.entity.Member;
import com.blibli.memberModule.repository.MemberRepository;
import com.blibli.memberModule.service.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class MemberServiceImpl implements MemberService {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public MemberResponseDto register(MemberRequestDto request) {
        log.info("Registering new member with email: {}", request.getEmail());
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }
        Member member = new Member();
        member.setEmail(request.getEmail());
        member.setPassword(passwordEncoder.encode(request.getPassword()));
        member.setName(request.getName());
        member.setPhone(request.getPhone());
        member.setCreatedAt(LocalDateTime.now());
        member.setUpdatedAt(LocalDateTime.now());
        Member savedMember = memberRepository.save(member);
        log.info("Member registered successfully with id: {}", savedMember.getMemberId());
        return convertToResponse(savedMember);
    }

    @Override
    public LoginResponseDto login(LoginRequestDto request) {
        log.info("Login attempt for email: {}", request.getEmail());
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));
        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }
        log.info("Login successful for member id: {}", member.getMemberId());
        LoginResponseDto loginResponseDto = new LoginResponseDto();
        loginResponseDto.setMember(convertToResponse(member));
        return loginResponseDto;
    }

    @Override
    public void logout(Long memberId) {
        log.info("Logout request for member id: {}", memberId);
        memberRepository.findById(String.valueOf(memberId))
                .orElseThrow(() -> new RuntimeException("Member not found with id: " + memberId));
        log.info("Logout successful for member id: {}", memberId);
    }

    private MemberResponseDto convertToResponse(Member member) {
        MemberResponseDto memberResponseDto = new MemberResponseDto();
        memberResponseDto.setMemberId(member.getMemberId());
        memberResponseDto.setEmail(member.getEmail());
        memberResponseDto.setName(member.getName());
        memberResponseDto.setPhone(member.getPhone());
        return memberResponseDto;
    }

}
