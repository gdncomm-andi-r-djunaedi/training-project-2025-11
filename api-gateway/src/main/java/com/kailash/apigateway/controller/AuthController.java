package com.kailash.apigateway.controller;

import com.kailash.apigateway.dto.ApiResponse;
import com.kailash.apigateway.dto.LoginRequest;
import com.kailash.apigateway.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService svc;

    public AuthController(AuthService svc) {
        this.svc = svc;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@RequestBody LoginRequest req) {
        ApiResponse<Map<String, Object>> data = svc.login(req);
        return ResponseEntity.ok(ApiResponse.success(data.getData()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String, Object>>> refresh(@RequestBody Map<String, String> body) {
        String refreshId = body.get("refreshTokenId");
        ApiResponse<Map<String, Object>> data = svc.refresh(refreshId);
        return ResponseEntity.ok(ApiResponse.success(data.getData()));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestBody Map<String, String> body) {
        String refreshId = body.get("refreshTokenId");
        svc.logout(refreshId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
