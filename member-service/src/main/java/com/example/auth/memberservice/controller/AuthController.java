package com.example.auth.memberservice.controller;

import com.example.auth.memberservice.dto.LoginRequest;
import com.example.auth.memberservice.dto.LoginResponse;
import com.example.auth.memberservice.dto.MemberRegisterRequest;
import com.example.auth.memberservice.dto.MemberRegisterResponse;
import com.example.auth.memberservice.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final MemberService memberService;

  @PostMapping("/register")
  public ResponseEntity<MemberRegisterResponse> register(@Valid @RequestBody MemberRegisterRequest request) {
    return ResponseEntity.ok(memberService.register(request));
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    return ResponseEntity.ok(memberService.login(request));
  }

}
