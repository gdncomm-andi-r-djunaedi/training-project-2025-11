package com.gdn.training.apigateway.controller;

import com.gdn.training.apigateway.dto.AuthResponse;
import com.gdn.training.apigateway.dto.BaseResponse;
import com.gdn.training.apigateway.dto.AuthRequest;
import com.gdn.training.apigateway.service.AuthService;
import com.gdn.training.apigateway.service.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    private final com.gdn.training.apigateway.service.JwtService jwtService;

    @PostMapping("/generate-token")
    public String generateToken(@RequestParam String username) {
        return jwtService.generateToken(username);
    }

    @PostMapping("/login")
    public ResponseEntity<BaseResponse<AuthResponse>> login(@RequestBody AuthRequest authRequest) {
        AuthResponse authResponse = authService.login(authRequest.getUsername(), authRequest.getPassword());
        BaseResponse<AuthResponse> response = BaseResponse.<AuthResponse>builder()
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK.name())
                .data(authResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public String logout(@RequestParam String token) {
        jwtService.invalidateToken(token);
        return "Logged out";
    }
}
