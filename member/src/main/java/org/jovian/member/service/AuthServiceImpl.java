package org.jovian.member.service;

import lombok.RequiredArgsConstructor;
import org.jovian.member.dto.LoginRequest;
import org.jovian.member.dto.RegisterRequest;
import org.jovian.member.exception.InvalidLoginException;
import org.jovian.member.exception.MemberAlreadyExistsException;
import org.jovian.member.exception.MemberNotFoundException;
import org.jovian.member.repository.MemberRepository;
import org.jovian.member.entity.Member;
import org.jovian.member.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public void register(RegisterRequest request) {
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new MemberAlreadyExistsException("Email already in use");
        }

        if (memberRepository.existsByUsername(request.getUsername())) {
            throw new MemberAlreadyExistsException("Username already in use");
        }

        Member member = Member.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .build();

        memberRepository.save(member);
    }

    @Override
    public String login(LoginRequest request) {
        Member member = memberRepository.findByEmailOrUsername(request.getIdentifier(), request.getIdentifier()).orElseThrow(() -> new MemberNotFoundException("Member not found"));

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return jwtService.generateToken(member);
    }
}