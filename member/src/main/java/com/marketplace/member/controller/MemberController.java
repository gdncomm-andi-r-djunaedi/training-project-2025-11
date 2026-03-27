package com.marketplace.member.controller;

import com.marketplace.member.dto.LoginRequest;
import com.marketplace.member.dto.MemberResponse;
import com.marketplace.member.dto.RegisterRequest;
import com.marketplace.member.service.MemberService;
import com.marketplace.member.util.ApiResponse;
import com.marketplace.member.util.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<MemberResponse>> register(@Valid @RequestBody RegisterRequest request) {
        MemberResponse response = memberService.register(request);
        return ResponseUtil.created(response, "Member registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<MemberResponse>> login(@Valid @RequestBody LoginRequest request) {
        MemberResponse response = memberService.login(request);
        return ResponseUtil.success(response, "Login successful");
    }
}
