package com.blibli.memberModule.services.impl;

import com.blibli.memberModule.dto.CheckUserResponse;
import com.blibli.memberModule.dto.Memberdto;
import com.blibli.memberModule.entity.Member;
import com.blibli.memberModule.exception.EmailAlreadyExistsException;
import com.blibli.memberModule.exception.ValidationException;
import com.blibli.memberModule.repository.MemberRepository;
import com.blibli.memberModule.services.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class MemberServiceImpl implements MemberService {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public Memberdto registerMember(Memberdto memberdto) {
        log.info("Registering new member with email: {}", memberdto.getEmail());
        
        if (memberdto.getEmail() == null || memberdto.getEmail().trim().isEmpty()) {
            log.warn("Registration failed: Missing email field");
            throw new ValidationException("MISSING_FIELDS", "Required fields missing: email");
        }
        if (memberdto.getFull_name() == null || memberdto.getFull_name().trim().isEmpty()) {
            log.warn("Registration failed for email {}: Missing full_name field", memberdto.getEmail());
            throw new ValidationException("MISSING_FIELDS", "Required fields missing: full_name");
        }
        if (memberdto.getPassword() == null || memberdto.getPassword().trim().isEmpty()) {
            log.warn("Registration failed for email {}: Missing password field", memberdto.getEmail());
            throw new ValidationException("MISSING_FIELDS", "Required fields missing: password");
        }
        if (memberdto.getPhoneNo() == null || memberdto.getPhoneNo().trim().isEmpty()) {
            log.warn("Registration failed for email {}: Missing phoneNo field", memberdto.getEmail());
            throw new ValidationException("MISSING_FIELDS", "Required fields missing: phoneNo");
        }
        
        String phoneNo = memberdto.getPhoneNo().trim();
        if (!phoneNo.matches("^\\d{10}$")) {
            log.warn("Registration failed for email {}: Invalid phone number format", memberdto.getEmail());
            throw new ValidationException("INVALID_PHONE_NUMBER", "Invalid phone number: Phone number must be exactly 10 digits");
        }
        
        String emailPattern = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,10}$";
        if (!memberdto.getEmail().matches(emailPattern)) {
            log.warn("Registration failed for email {}: Invalid email format", memberdto.getEmail());
            throw new ValidationException("INVALID_EMAIL_FORMAT", "Invalid email format");
        }
        
        String password = memberdto.getPassword();
        if (password.length() < 8 || password.length() > 20) {
            log.warn("Registration failed for email {}: Password length invalid", memberdto.getEmail());
            throw new ValidationException("WEAK_PASSWORD", "Weak password: Password must be 8-20 characters");
        }
        if (!password.matches("^(?=.*[A-Z])(?=.*[0-9])(?=.*[a-z])(?=.*[@#$%^&+=!]).*$")) {
            log.warn("Registration failed for email {}: Password does not meet requirements", memberdto.getEmail());
            throw new ValidationException("WEAK_PASSWORD", "Weak password: Password must contain atleast uppercase, lowercase, number, and special character");
        }
        
        Optional<Member> existingMember = memberRepository.findByEmail(memberdto.getEmail());
        if (existingMember.isPresent()) {
            log.warn("Registration failed for email {}: Email already exists", memberdto.getEmail());
            throw new EmailAlreadyExistsException("Email already registered");
        }
        
        log.debug("All validations passed for email: {}, encoding password", memberdto.getEmail());
        String encodedPassword = passwordEncoder.encode(memberdto.getPassword());
        memberdto.setPassword(encodedPassword);
        Member member = new Member();
        BeanUtils.copyProperties(memberdto, member);
        member.setId(null);
        Member save = memberRepository.save(member);
        log.info("Member registered successfully with email: {}", memberdto.getEmail());
        return convert(save);
    }

    @Override
    public CheckUserResponse validateMember(String email, String password) {
        log.debug("Validating member credentials for email: {}", email);
        Optional<Member> memberOpt = memberRepository.findByEmail(email);

        if (memberOpt.isEmpty()) {
            log.warn("Member validation failed: User not found with email: {}", email);
            return new CheckUserResponse("NOT_FOUND", email, "User not found with the provided email");
        }
        Member member = memberOpt.get();

        if (!passwordEncoder.matches(password, member.getPassword())) {
            log.warn("Member validation failed: Incorrect password for email: {}", email);
            return new CheckUserResponse("INVALID_PASSWORD", email, "Password is incorrect");
        }

        log.info("Member validation successful for email: {}", email);
        return new CheckUserResponse("VALID", email, "User credentials are valid");
    }

    @Override
    public Memberdto getMemberByEmail(String email) {
        log.debug("Fetching member details for email: {}", email);
        Optional<Member> memberOpt = memberRepository.findByEmail(email);
        if (memberOpt.isEmpty()) {
            log.warn("Member not found with email: {}", email);
            throw new RuntimeException("Member not found with email: " + email);
        }
        log.info("Member details retrieved successfully for email: {}", email);
        return convert(memberOpt.get());
    }

    private Memberdto convert(Member member) {
        Memberdto memberdto = new Memberdto();
        BeanUtils.copyProperties(member, memberdto);
        memberdto.setPassword(null);
        return memberdto;
    }
}
