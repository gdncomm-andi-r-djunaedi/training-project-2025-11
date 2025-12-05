package com.training.member.memberassignment.service.impl;

import com.training.member.memberassignment.dto.InputDTO;
import com.training.member.memberassignment.dto.OutputDTO;
import com.training.member.memberassignment.entity.MemberEntity;
import com.training.member.memberassignment.exception.MemberException;
import com.training.member.memberassignment.repository.MemberRepository;
import com.training.member.memberassignment.service.MemberService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberServiceImpl(MemberRepository memberRepository,
            PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void register(InputDTO inputDTO) {
        if (inputDTO.getEmail() == null || inputDTO.getEmail().trim().isEmpty() || inputDTO.getPassword() == null || inputDTO.getPassword().trim().isEmpty()) {
            throw MemberException.invalidPayload();
        }
        if (memberRepository.existsByEmail(inputDTO.getEmail())) {
            throw MemberException.emailAlreadyExists(inputDTO.getEmail());
        }
        String hashedPassword = passwordEncoder.encode(inputDTO.getPassword());
        MemberEntity member = MemberEntity.builder().email(inputDTO.getEmail()).passwordHash(hashedPassword).build();
        memberRepository.save(member);
    }

    @Override
    public OutputDTO login(InputDTO inputDTO) {
        MemberEntity memberEntity = memberRepository.findByEmail(inputDTO.getEmail()).orElseThrow(MemberException::invalidCredentials);
        if (!passwordEncoder.matches(inputDTO.getPassword(), memberEntity.getPasswordHash())) {
            throw MemberException.invalidCredentials();
        }
        return OutputDTO.builder().email(memberEntity.getEmail()).build();
    }
}
