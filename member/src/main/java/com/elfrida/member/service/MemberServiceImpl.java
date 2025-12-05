package com.elfrida.member.service;

import com.elfrida.member.configuration.JwtUtil;
import com.elfrida.member.dto.LoginRequest;
import com.elfrida.member.dto.LoginResponse;
import com.elfrida.member.dto.MemberRequest;
import com.elfrida.member.exception.EmailAlreadyRegisteredException;
import com.elfrida.member.exception.InvalidCredentialsException;
import com.elfrida.member.model.Member;
import com.elfrida.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public Member register(MemberRequest request) {
        if (memberRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyRegisteredException(request.getEmail());
        }

        Member member = new Member();
        member.setName(request.getName());
        member.setEmail(request.getEmail());
        member.setPassword(passwordEncoder.encode(request.getPassword()));

        return memberRepository.save(member);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtUtil.generateToken(member.getEmail());
        return new LoginResponse(member.getName(), token);
    }

    @Override
    public void logout(String token) {
        // Saat ini kita menggunakan JWT stateless, jadi "logout" dilakukan di sisi client
        // (hapus token dari penyimpanan). Di sini disediakan hook untuk:
        // - Menambahkan token ke blacklist (jika nanti disimpan di DB/Redis)
        // - Mencatat log aktivitas logout, dsb.
        // Untuk sekarang, method ini tidak melakukan apa-apa.
    }
}
