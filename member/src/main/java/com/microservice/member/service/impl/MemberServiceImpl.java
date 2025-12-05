package com.microservice.member.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservice.member.dto.LoginRequestDto;
import com.microservice.member.dto.MemberLogInResponseDto;
import com.microservice.member.dto.RegisterRequestDto;
import com.microservice.member.dto.RegisterResponseDto;
import com.microservice.member.entity.Member;
import com.microservice.member.exceptions.BusinessException;
import com.microservice.member.exceptions.ValidationException;
import com.microservice.member.repository.MemberRepository;
import com.microservice.member.service.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Slf4j
@Service
public class MemberServiceImpl implements MemberService {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ObjectMapper objectMapper;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @Override
    @Transactional
    public RegisterResponseDto registerNewUser(RegisterRequestDto registerRequestDto) {
        log.info("Processing registration for email: {}", registerRequestDto.getEmail());

        try {
            if (memberRepository.existsByEmail(registerRequestDto.getEmail().trim().toLowerCase())) {
                log.warn("Registration failed: Email already exists - {}", registerRequestDto.getEmail());
                throw new BusinessException("Email address is already registered. Please use a different email or try logging in.");
            }

            if (memberRepository.existsByPhoneNumber(registerRequestDto.getPhoneNumber().trim())) {
                log.warn("Registration failed: Phone number already exists - {}", registerRequestDto.getPhoneNumber());
                throw new BusinessException("Phone number is already registered. Please use a different phone number.");
            }

            Member member = new Member();
            member.setName(registerRequestDto.getName().trim());
            member.setAddress(registerRequestDto.getAddress() != null && !registerRequestDto.getAddress().trim().isEmpty()
                    ? registerRequestDto.getAddress().trim()
                    : null);
            member.setPhoneNumber(registerRequestDto.getPhoneNumber().trim());
            member.setEmail(registerRequestDto.getEmail().trim().toLowerCase()); // Normalize email
            member.setCreatedAt(new Date());
            member.setPasswordHash(encoder.encode(registerRequestDto.getPassword()));

            Member savedMember = memberRepository.save(member);
            log.info("User registered successfully with ID: {} and email: {}", savedMember.getId(), savedMember.getEmail());

            RegisterResponseDto registerResponseDto = new RegisterResponseDto();
            registerResponseDto.setId(savedMember.getId());
            registerResponseDto.setName(savedMember.getName());
            registerResponseDto.setMessage("Registered the user successfully");
            registerResponseDto.setEmail(savedMember.getEmail());

            return registerResponseDto;

        } catch (BusinessException e) {
            log.error("Registration failed: {}", e.getMessage());
            throw e;
        } catch (DataIntegrityViolationException e) {
            log.error("Registration failed due to database constraint violation for email: {}", registerRequestDto.getEmail());
            if (e.getMessage() != null && (e.getMessage().contains("email") || e.getMessage().contains("uc_members_email"))) {
                throw new BusinessException("Email address is already registered. Please use a different email or try logging in.");
            } else if (e.getMessage() != null && (e.getMessage().contains("phone") || e.getMessage().contains("uc_members_phone"))) {
                throw new BusinessException("Phone number is already registered. Please use a different phone number.");
            } else {
                throw new BusinessException("Registration failed due to data integrity violation. Please try again.");
            }
        } catch (Exception e) {
            log.error("Unexpected error during registration: {}", e.getMessage());
            throw new BusinessException("Registration failed due to an unexpected error. Please try again later.");
        }
    }

    @Override
    @Transactional
    public MemberLogInResponseDto validateUser(LoginRequestDto loginRequestDto) {
        log.info("Validating login for email: {}", loginRequestDto.getEmail());

        try {
            Member member = memberRepository.findByEmail(loginRequestDto.getEmail());

            if (member == null) {
                log.warn("Login failed: User not found with email: {}", loginRequestDto.getEmail());
                throw new ValidationException("User Not Found");
            }

            if (!encoder.matches(loginRequestDto.getPassword(), member.getPasswordHash())) {
                log.warn("Login failed: Incorrect password for email: {}", loginRequestDto.getEmail());
                throw new ValidationException("Incorrect Password");
            }

            log.info("User logged in successfully with ID: {}", member.getId());
            MemberLogInResponseDto memberLogInResponseDto = new MemberLogInResponseDto();
            memberLogInResponseDto.setIsMember(Boolean.TRUE);
            memberLogInResponseDto.setUserId(member.getId());
            return memberLogInResponseDto;

        } catch (ValidationException e) {
            log.error("Login validation failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during login validation for email: {}", loginRequestDto.getEmail(), e);
            throw new ValidationException("Login failed due to an unexpected error. Please try again later.");
        }
    }
}