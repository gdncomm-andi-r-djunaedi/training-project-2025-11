package com.gdn.training.member_service.controller;

import com.gdn.training.member_service.dto.LoginRequest;
import com.gdn.training.member_service.dto.LoginResponse;
import com.gdn.training.member_service.dto.MemberResponse;
import com.gdn.training.member_service.dto.RegisterRequest;
import com.gdn.training.member_service.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
@Tag(name = "Member", description = "member management APIs")
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/register")
    @Operation(summary = "Register new member", description = "Create a new member account")
    public ResponseEntity<MemberResponse> register(@Valid @RequestBody RegisterRequest request) {
        MemberResponse response = memberService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Member login", description = "Authenticate and receive JWT token")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = memberService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(
            summary = "Get current member profile",
            description = "Get profile of the authenticated member",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    public ResponseEntity<MemberResponse> getCurrentMember(@RequestHeader("X-Member-Id") Long memberId){
        MemberResponse response = memberService.getMemberById(memberId);
        return ResponseEntity.ok(response);
    }
}
