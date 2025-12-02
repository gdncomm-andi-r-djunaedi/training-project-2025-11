package com.blibli.training.member.controller;

import com.blibli.training.member.dto.LoginRequest;
import com.blibli.training.member.dto.MemberResponse;
import com.blibli.training.member.dto.RegisterRequest;
import com.blibli.training.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/register")
    public ResponseEntity<MemberResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(memberService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<MemberResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(memberService.login(request));
    }

    @GetMapping
    public ResponseEntity<java.util.List<MemberResponse>> getAllMembers() {
        return ResponseEntity.ok(memberService.findAll());
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        memberService.logout(token);
        return ResponseEntity.ok("Logged out successfully");
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<MemberResponse> findByUsername(@PathVariable String username) {
        return ResponseEntity.ok(memberService.findByUsername(username));
    }
}
