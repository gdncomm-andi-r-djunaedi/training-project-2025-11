package com.example.marketplace.member.controller;

import com.example.marketplace.common.dto.ApiResponse;
import com.example.marketplace.member.dto.LoginRequestDTO;
import com.example.marketplace.member.dto.MemberResponseDTO;
import com.example.marketplace.member.dto.RegisterRequestDTO;
import com.example.marketplace.member.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/internal/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) { this.memberService = memberService; }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<MemberResponseDTO>> register(@Valid @RequestBody RegisterRequestDTO req) {
        MemberResponseDTO dto = memberService.register(req);
        return ResponseEntity.status(201).body(ApiResponse.ok(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, String>>> login(@Valid @RequestBody LoginRequestDTO req) {
        String token = memberService.login(req);
        Map<String, String> body = Collections.singletonMap("token", token);
        return ResponseEntity.ok(ApiResponse.ok(body));
    }

    @GetMapping("/{id}/exists")
    public Boolean exists(@PathVariable String id) {
        return memberService.existsById(id);
    }
}
