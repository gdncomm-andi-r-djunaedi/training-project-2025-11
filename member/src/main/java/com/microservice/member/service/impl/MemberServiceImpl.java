package com.microservice.member.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservice.member.dto.LoginRequestDto;
import com.microservice.member.dto.MemberLogInResponseDto;
import com.microservice.member.dto.RegisterRequestDto;
import com.microservice.member.dto.RegisterResponseDto;
import com.microservice.member.entity.Member;
import com.microservice.member.exceptions.ValidationException;
import com.microservice.member.repository.MemberRepository;
import com.microservice.member.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class MemberServiceImpl implements MemberService {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ObjectMapper objectMapper;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @Override
    public RegisterResponseDto registerNewUser(RegisterRequestDto registerRequestDto) {
        Member member = new Member();
        member.setName(registerRequestDto.getName());
        member.setAddress(registerRequestDto.getAddress());
        member.setPhoneNumber(registerRequestDto.getPhoneNumber());
        member.setEmail(registerRequestDto.getEmail());
        member.setCreatedAt(new Date());
        member.setPasswordHash(encoder.encode(registerRequestDto.getPassword()));

        Member savedMember = memberRepository.save(member);
        RegisterResponseDto registerResponseDto = new RegisterResponseDto();
        registerResponseDto.setId(savedMember.getId());
        registerResponseDto.setName(savedMember.getName());
        registerResponseDto.setMessage("Registerd the user sucessfully");
        registerResponseDto.setEmail(savedMember.getEmail());
        return registerResponseDto;
    }

    @Override
    public MemberLogInResponseDto validateUser(LoginRequestDto loginRequestDto) {
        Member member = memberRepository.findByEmail(loginRequestDto.getEmail());
        if (member == null) {
            throw new ValidationException("User Not Found");
        }
        if (member.getEmail().equalsIgnoreCase(loginRequestDto.getEmail())) {
            if (encoder.matches(loginRequestDto.getPassword(), member.getPasswordHash())) {
                MemberLogInResponseDto memberLogInResponseDto = new MemberLogInResponseDto();
                memberLogInResponseDto.setIsMember(Boolean.TRUE);
                memberLogInResponseDto.setUserId(member.getId());
                return memberLogInResponseDto;
            }
            throw new ValidationException("Incorrect Password");
        }
        throw new ValidationException("User Not Found");
    }
}
