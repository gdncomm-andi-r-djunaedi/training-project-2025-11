package com.ecommerce.member.controller;

import com.ecommerce.member.dto.LoginDto;
import com.ecommerce.member.dto.LoginResponseDto;
import com.ecommerce.member.dto.MemberDto;
import com.ecommerce.member.entity.Member;
import com.ecommerce.member.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody MemberDto memberDto) {
        memberService.register(memberDto);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginDto loginDto) {
        Member member = memberService.login(loginDto);
        LoginResponseDto response = new LoginResponseDto(
                member.getId(),
                member.getUsername(),
                member.getEmail(),
                member.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        // invalidating token will be handled on api gateway
        return ResponseEntity.ok("Logged out successfully");
    }
}
