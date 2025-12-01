package com.example.gateway.web;

import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Map;

@RestController
public class LogoutController {

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