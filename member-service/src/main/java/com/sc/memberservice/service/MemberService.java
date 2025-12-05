package com.sc.memberservice.service;

import com.sc.memberservice.client.CartFeign;
import com.sc.memberservice.dto.LoginDto;
import com.sc.memberservice.dto.RegisterDto;
import com.sc.memberservice.model.Member;
import com.sc.memberservice.repository.MemberRepository;
import com.sc.memberservice.utils.JwtUtil;
import com.sc.utilsservice.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final CartFeign cartFeign;

    public ApiResponse<String> register(RegisterDto registerDto) {

        if (memberRepository.findByEmail(registerDto.getEmail()).isPresent()) {
            return ApiResponse.failure("EMAIL_ALREADY_EXISTS", "Email already registered.");
        }

        Member member = new Member();
        member.setUsername(registerDto.getUsername());
        member.setEmail(registerDto.getEmail());
        member.setPassword(bCryptPasswordEncoder.encode(registerDto.getPassword()));

        Member savedMember = memberRepository.save(member);

        cartFeign.createCart(savedMember.getMemberId());

        return ApiResponse.success("Member registered successfully with email: " + savedMember.getEmail());
    }

    public Pair<String, ApiResponse<String>> login(LoginDto loginDto) {
        Member member;
        if (memberRepository.findByEmail(loginDto.getEmail()).isEmpty()) {
            return new Pair<>(null, ApiResponse.failure("EMAIL_NOT_FOUND", "Email not found."));
        } else {
            member = memberRepository.findByEmail(loginDto.getEmail()).get();
        }

        boolean passwordMatches = bCryptPasswordEncoder.matches(loginDto.getPassword(), member.getPassword());
        if (!passwordMatches) {
            return new Pair<>(
                    null,
                    ApiResponse.failure("INVALID_CREDENTIALS", "Invalid email or password.")
            );
        }

        return new Pair<>(
                jwtUtil.generateToken(member.getMemberId() + ":" + loginDto.getEmail()),
                ApiResponse.success("Member logged in successfully.")
        );
    }
}
