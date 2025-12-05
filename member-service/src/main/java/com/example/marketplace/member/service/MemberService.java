package com.example.marketplace.member.service;

import com.example.marketplace.common.dto.JwtPayloadDTO;
import com.example.marketplace.member.domain.Member;
import com.example.marketplace.member.dto.LoginRequestDTO;
import com.example.marketplace.member.dto.MemberResponseDTO;
import com.example.marketplace.member.dto.RegisterRequestDTO;
import com.example.marketplace.member.mapper.MemberMapper;
import com.example.marketplace.member.repo.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MemberService {

    private final MemberRepository repo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public MemberService(MemberRepository repo, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public MemberResponseDTO register(RegisterRequestDTO req) {
        if (repo.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("email exists");
        }
        if (repo.existsByUsername(req.getUsername())) {
            throw new IllegalArgumentException("username exists");
        }
        String hash = passwordEncoder.encode(req.getPassword());
        Member m = new Member(req.getUsername(), req.getEmail(), hash);
        repo.save(m);
        return MemberMapper.toDto(m);
    }

    public String login(LoginRequestDTO req) {
        Optional<Member> opt = repo.findByEmail(req.getEmail());
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("invalid credentials");
        }
        Member m = opt.get();
        if (!passwordEncoder.matches(req.getPassword(), m.getPasswordHash())) {
            throw new IllegalArgumentException("invalid credentials");
        }
        JwtPayloadDTO payload = new JwtPayloadDTO(m.getId(), m.getUsername());
        return jwtService.generateToken(payload);
    }

    public boolean existsById(String id) {
        return repo.existsById(id);
    }
}
