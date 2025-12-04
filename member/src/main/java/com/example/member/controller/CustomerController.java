package com.example.member.controller;

import com.example.member.dto.AuthResponse;
import com.example.member.dto.LoginRequest;
import com.example.member.dto.RegisterRequest;
import com.example.member.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/member")
public class CustomerController {
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = customerService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = customerService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String, String>> validate(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            Map<String, String> error = new HashMap<>();
            error.put("status", "invalid");
            error.put("message", "Authentication token is missing or invalid");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("email", authentication.getName());
        userInfo.put("status", "valid");
        return ResponseEntity.ok(userInfo);
    }
}

