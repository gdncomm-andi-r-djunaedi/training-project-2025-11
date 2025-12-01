package com.example.member.controller;

import com.example.common.security.JwtService;
import com.example.member.entity.Member;
import com.example.member.model.LoginRequest;
import com.example.member.model.RegisterRequest;
import com.example.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

  private final MemberService memberService;
  private final JwtService jwtService;

  @PostMapping("/register")
  public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
    Member saved = memberService.register(req.email(), req.password(), req.fullName());
    return ResponseEntity.ok(Map.of("id", saved.getId()));
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginRequest req) {
    Member m = memberService.authenticate(req.email(), req.password());

    String token = jwtService.generateToken(
        m.getId().toString(),
        Map.of("email", m.getEmail()),
        3600
    );

    ResponseCookie cookie = ResponseCookie.from("JWT", token)
        .httpOnly(true)
        .secure(false)
        .path("/")
        .maxAge(Duration.ofHours(1))
        .sameSite("Strict")
        .build();

    return ResponseEntity.ok()
        .header("Set-Cookie", cookie.toString())
        .body(Map.of("token", token));
  }

  @PostMapping("/logout")
  public ResponseEntity<?> logout() {
    ResponseCookie clearCookie = ResponseCookie.from("JWT", "")
        .httpOnly(true)
        .secure(false)
        .path("/")
        .maxAge(Duration.ZERO)
        .sameSite("Strict")
        .build();

    return ResponseEntity.ok()
        .header("Set-Cookie", clearCookie.toString())
        .body(Map.of("message", "Logged out, JWT cookie cleared"));
  }
}