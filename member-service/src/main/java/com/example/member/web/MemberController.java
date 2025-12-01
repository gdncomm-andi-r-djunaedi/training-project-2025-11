package com.example.member.web;

import com.example.common.command.CommandExecutor;
import com.example.common.security.JwtService;
import com.example.member.domain.Member;
import com.example.member.repository.MemberRepository;
import com.example.member.service.RegisterMemberCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

  private final MemberRepository repo;
  private final PasswordEncoder encoder;
  private final JwtService jwtService;
  private final CommandExecutor executor;

  @PostMapping("/register")
  public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
    Member saved =
        executor.execute(new RegisterMemberCommand(repo, encoder, req.email(), req.password(), req.fullName()));
    return ResponseEntity.ok(Map.of("id", saved.getId()));
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginRequest req) {
    Member m = repo.findByEmail(req.email()).orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
    if (!encoder.matches(req.password(), m.getPasswordHash())) {
      throw new IllegalArgumentException("Invalid credentials");
    }

    String token = jwtService.generateToken(m.getId().toString(), Map.of("email", m.getEmail()), 3600);

    ResponseCookie cookie = ResponseCookie.from("JWT", token)
        .httpOnly(true)
        .secure(false)
        .path("/")
        .maxAge(Duration.ofHours(1))
        .sameSite("Strict")
        .build();

    return ResponseEntity.ok().header("Set-Cookie", cookie.toString()).body(Map.of("token", token));
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

  public record RegisterRequest(String email, String password, String fullName) {
  }


  public record LoginRequest(String email, String password) {
  }
}