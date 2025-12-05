package com.training.member.memberassignment.controller;

import com.training.member.memberassignment.dto.ApiResponse;
import com.training.member.memberassignment.dto.InputDTO;
import com.training.member.memberassignment.dto.OutputDTO;
import com.training.member.memberassignment.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/member")
@Tag(name = "Member Management", description = "APIs for member registration, authentication, and profile management")
public class MemberController {
    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new member", description = "Register a new member with email and password. Password must be at least 8 characters with one uppercase letter and one digit.")
    public ResponseEntity<ApiResponse<Void>> registerUser(@Valid @RequestBody InputDTO inputDTO) {
        memberService.register(inputDTO);
        return ResponseEntity.ok(ApiResponse.success("Registration successful"));
    }

    @PostMapping("/login")
    @Operation(summary = "Member login", description = "Authenticate member with email and password. Returns JWT access token and refresh token.")
    public ResponseEntity<ApiResponse<OutputDTO>> login(@Valid @RequestBody InputDTO inputDTO) {
        OutputDTO loginResponse = memberService.login(inputDTO);
        return ResponseEntity.ok(ApiResponse.success("Login successful", loginResponse));
    }
}