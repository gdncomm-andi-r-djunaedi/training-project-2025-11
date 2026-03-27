package com.ecom.member.Service.Impl;

import com.ecom.member.Config.SecurityConfig;
import com.ecom.member.Dto.MemberDto;
import com.ecom.member.Entity.Member;
import com.ecom.member.Repository.MemberRepo;
import com.ecom.member.Service.MemberService;
import com.ecom.member.exception.WrongCredsException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Random;
import java.util.UUID;

@Service
public class MemberServiceImpl implements MemberService {

    @Autowired
    MemberRepo memberRepo;

    @Autowired
    SecurityConfig securityConfig;

    @Override
    public boolean register(MemberDto memberDto) {

        if (memberRepo.existsByEmail(memberDto.getEmail())) {
            throw new WrongCredsException("Email already exists");
        }
        Member member = new Member();
        BeanUtils.copyProperties(memberDto,member);
        member.setPassword(securityConfig.passwordEncoder().encode(memberDto.getPassword()));

        memberRepo.save(member);

        return true;

    }

    @Override
    public String login(String email, String password) {

        Member member = memberRepo.findByEmail(email)
                .orElseThrow(() -> new WrongCredsException("User not found"));

        boolean matches = securityConfig.passwordEncoder().matches(password, member.getPassword());
        if (!matches) {
            throw new WrongCredsException("Invalid credentials");
        }

        return member.getUserId();
    }

    @Override
    public String logout(String userId) {
        return "Logged out user: "+userId+"";
    }

}
