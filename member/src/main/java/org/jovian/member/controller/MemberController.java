package org.jovian.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.jovian.member.dto.LoginRequest;
import org.jovian.member.dto.RegisterRequest;
import org.jovian.member.dto.LoginResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.jovian.member.service.AuthService;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {
    private final AuthService authService;


    @GetMapping("/home")
    @Operation(summary="Homepage")
    public String home() {
        return "Member Service is running!";
    }

    @PostMapping("/register")
    @Operation(summary="Register New User")
    public ResponseEntity<String> register(@RequestBody RegisterRequest registerRequest) {
        authService.register(registerRequest);
        return ResponseEntity.ok("Member registered successfully");
    }

    @PostMapping("/login")
    @Operation(summary="Login User")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        String token = authService.login(loginRequest);
        LoginResponse response = new LoginResponse(token);
        return ResponseEntity.ok(response);
    }
}
