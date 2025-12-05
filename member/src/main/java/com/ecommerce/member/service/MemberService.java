package com.ecommerce.member.service;

import com.ecommerce.member.dto.LoginDto;
import com.ecommerce.member.dto.MemberDto;
import com.ecommerce.member.entity.Member;
import com.ecommerce.member.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(MemberDto memberDto) {
        if (memberRepository.findByUsername(memberDto.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        Member member = new Member();
        member.setUsername(memberDto.getUsername());
        member.setPassword(passwordEncoder.encode(memberDto.getPassword()));
        member.setEmail(memberDto.getEmail());
        member.setName(memberDto.getName());
        memberRepository.save(member);
    }

    public Member login(LoginDto loginDto) {
        Member member = memberRepository.findByUsername(loginDto.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(loginDto.getPassword(), member.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return member;
    }
}
