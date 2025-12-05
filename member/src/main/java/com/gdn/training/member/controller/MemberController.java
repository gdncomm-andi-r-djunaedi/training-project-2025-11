package com.gdn.training.member.controller;

import com.gdn.training.member.dto.AuthResponse;
import com.gdn.training.member.dto.LoginRequest;
import com.gdn.training.member.dto.RegisterRequest;
import com.gdn.training.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
@Tag(name = "Member Controller", description = "APIs for member authentication and management")
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "Register new member", description = "Register a new member with username, email, and password")
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid RegisterRequest request) {
        return ResponseEntity.ok(memberService.register(request));
    }

    @Operation(summary = "Login member", description = "Login with username and password to get JWT token")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        AuthResponse authResponse = memberService.login(request);

        ResponseCookie cookie = ResponseCookie.from("jwt", authResponse.getToken())
                .httpOnly(true)
                .secure(false) // Set to true in production
                .path("/")
                .maxAge(30 * 60) // 30 minutes
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(authResponse);
    }

    @Operation(summary = "Logout member", description = "Logout by invalidating the JWT token")
    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @Parameter(description = "Bearer token") @org.springframework.web.bind.annotation.RequestHeader("Authorization") String token) {
        memberService.logout(token);
        return ResponseEntity.ok("Logged out successfully");
    }
}
