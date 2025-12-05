package com.gdn.training.apigateway.controller;

import com.gdn.training.apigateway.security.JwtUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    // Example usage: GET /auth/token?user=steffani
    @GetMapping("/auth/token")
    public String getToken(@RequestParam String user) {
        return jwtUtil.generateToken(user);
    }
}
