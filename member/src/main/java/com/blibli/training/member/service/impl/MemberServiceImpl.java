package com.blibli.training.member.service.impl;

import com.blibli.training.member.dto.LoginRequest;
import com.blibli.training.member.dto.MemberResponse;
import com.blibli.training.member.dto.RegisterRequest;
import com.blibli.training.member.entity.Member;
import com.blibli.training.member.repository.MemberRepository;
import com.blibli.training.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.blibli.training.member.client.AuthClient authClient;

    @Override
    public MemberResponse register(RegisterRequest request) {
        if (memberRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        Member member = Member.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        Member savedMember = memberRepository.save(member);

        return MemberResponse.builder()
                .id(savedMember.getId())
                .username(savedMember.getUsername())
                .email(savedMember.getEmail())
                .build();
    }

    @Override
    public MemberResponse login(LoginRequest request) {
        Member member = memberRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        // Generate JWT token via API Gateway
        String token = authClient.generateToken(member.getUsername());

        return MemberResponse.builder()
                .id(member.getId())
                .username(member.getUsername())
                .email(member.getEmail())
                .token(token)
                .build();
    }

    @Override
    public java.util.List<MemberResponse> findAll() {
        return memberRepository.findAll().stream()
                .map(member -> MemberResponse.builder()
                        .id(member.getId())
                        .username(member.getUsername())
                        .email(member.getEmail())
                        .build())
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public void logout(String token) {
        authClient.logout(token);
    }

    @Override
    public MemberResponse findByUsername(String username) {
        com.blibli.training.member.entity.Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        return MemberResponse.builder()
                .id(member.getId())
                .username(member.getUsername())
                .email(member.getEmail())
                .build();
    }
}
