package com.blibli.memberModule.controller;

import com.blibli.memberModule.dto.*;
import com.blibli.memberModule.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Slf4j
@Tag(name = "Member Management", description = "APIs for member registration and login")
@RestController
@RequestMapping("/api/members")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @Operation(summary = "Register a new member", description = "Create a new member account with email and password")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<MemberResponseDto>> register(@Valid @RequestBody MemberRequestDto memberRequestDto) {
        log.info("POST /api/members/register - email: {}", memberRequestDto.getEmail());
        MemberResponseDto memberResponseDto = memberService.register(memberRequestDto);
        return new ResponseEntity<>(ApiResponse.success(memberResponseDto), HttpStatus.OK);
    }

    @Operation(summary = "Member login", description = "Authenticate member with email and password")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        log.info("POST /api/members/login - email: {}", loginRequestDto.getEmail());
        LoginResponseDto loginResponseDto = memberService.login(loginRequestDto);
        return new ResponseEntity<>(ApiResponse.success(loginResponseDto), HttpStatus.OK);
    }

    @Operation(summary = "Member logout", description = "Logout member")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(@RequestParam Long memberId) {
        log.info("POST /api/members/logout - memberId: {}", memberId);
        memberService.logout(memberId);
        return new ResponseEntity<>(ApiResponse.success("Logout successful"), HttpStatus.OK);
    }

}
