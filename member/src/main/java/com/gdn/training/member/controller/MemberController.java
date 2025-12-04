package com.gdn.training.member.controller;

import com.gdn.training.member.dto.LoginRequest;
import com.gdn.training.member.dto.LoginResponse;
import com.gdn.training.member.dto.RegisterMemberRequest;
import com.gdn.training.member.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterMemberRequest request) {
        memberService.register(request.getUsername(), request.getEmail(), request.getPassword());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = memberService.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(jakarta.servlet.http.HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            memberService.logout(token);
            return ResponseEntity.ok(Map.of("message", "Logout successful"));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Invalid token"));
    }
}
