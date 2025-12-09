package com.gdn.training.member.controller;

import com.gdn.training.member.dto.LoginRequest;
import com.gdn.training.member.dto.LoginResponse;
import com.gdn.training.member.dto.LogoutResponse;
import com.gdn.training.member.dto.RegisterMemberRequest;
import com.gdn.training.member.dto.RegisterResponse;
import com.gdn.training.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterMemberRequest request) {
        RegisterResponse response = memberService.register(
                request.getUsername(),
                request.getEmail(),
                request.getPassword());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = memberService.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        LogoutResponse response = memberService.logout(authorizationHeader);
        return ResponseEntity.ok(response);
    }
}
