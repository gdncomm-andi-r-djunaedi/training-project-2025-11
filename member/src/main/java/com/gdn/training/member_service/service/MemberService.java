package com.gdn.training.member_service.service;

import com.gdn.training.member_service.dto.LoginRequest;
import com.gdn.training.member_service.dto.LoginResponse;
import com.gdn.training.member_service.dto.MemberResponse;
import com.gdn.training.member_service.dto.RegisterRequest;
import com.gdn.training.member_service.entity.Member;
import com.gdn.training.member_service.exception.InvalidCredentialsException;
import com.gdn.training.member_service.exception.MemberAlreadyExistsException;
import com.gdn.training.member_service.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public MemberResponse register(RegisterRequest request){
        if (memberRepository.existsByEmail(request.getEmail())){
            throw new MemberAlreadyExistsException("Member already registered: " + request.getEmail());
        }
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        Member member = Member.builder()
                .email(request.getEmail())
                .passwordHash(hashedPassword)
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .build();

        Member savedMember = memberRepository.save(member);

        return MemberResponse.builder()
                .id(savedMember.getId())
                .email(savedMember.getEmail())
                .fullName(savedMember.getFullName())
                .phoneNumber(savedMember.getPhoneNumber())
                .createdAt(savedMember.getCreatedAt())
                .build();
    }

    public LoginResponse login(LoginRequest request){
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        //if password and email not match:
        if(!passwordEncoder.matches(request.getPassword(), member.getPasswordHash())){
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(member.getId(), member.getEmail());

        return LoginResponse.builder()
                .token(token)
                .memberId(member.getId())
                .email(member.getEmail())
                .fullName(member.getFullName())
                .build();
    }

    public MemberResponse getMemberById(Long memberId){
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member Not Found"));

        return MemberResponse.builder()
                .id(member.getId())
                .email(member.getEmail())
                .fullName(member.getFullName())
                .phoneNumber(member.getPhoneNumber())
                .createdAt(member.getCreatedAt())
                .build();
    }

}
