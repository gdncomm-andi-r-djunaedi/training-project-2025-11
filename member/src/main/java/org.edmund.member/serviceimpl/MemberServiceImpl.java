package org.edmund.member.serviceimpl;

import lombok.RequiredArgsConstructor;
import org.edmund.commonlibrary.security.JwtService;
import org.edmund.member.dto.LoginMemberDto;
import org.edmund.member.dto.RegisterMemberDto;
import org.edmund.member.entity.Member;
import org.edmund.member.repository.MemberRepository;
import org.edmund.member.response.GetMemberResponse;
import org.edmund.member.response.LoginResponse;
import org.edmund.member.services.MemberService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public Member registerMember(RegisterMemberDto request) {
        memberRepository.findByEmail(request.getEmail()).ifPresent(m -> {
            throw new IllegalStateException("Email already exists");
        });

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        Member saved = Member.builder()
                .email(request.getEmail())
                .passwordHashed(encodedPassword)
                .fullName(request.getFullName())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        memberRepository.save(saved);
        return saved;
    }

    @Override
    public LoginResponse loginMember(LoginMemberDto request) {
        Member m = memberRepository.findByEmail(request.getEmail()).orElseThrow(() -> new IllegalStateException("Email not found"));
        if (!passwordEncoder.matches(request.getPassword(), m.getPasswordHashed())) {
            throw new IllegalStateException("Invalid Credentials");
        }
        String token = jwtService.generateToken(
                m.getId().toString(),
                Map.of("email", m.getEmail()),
                3600);

        return LoginResponse.builder()
                    .token(token)
                    .build();
    }

    @Override
    public Optional<GetMemberResponse> findByEmail(String email) {
        return memberRepository.findByEmail(email)
                .map(member -> GetMemberResponse.builder()
                        .id(member.getId())
                        .email(member.getEmail())
                        .fullName(member.getFullName())
                        .createdAt(member.getCreatedAt())
                        .updatedAt(member.getUpdatedAt())
                        .build()
                );
    }
}
