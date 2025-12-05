package com.blibli.AuthService.controller;

import com.blibli.AuthService.dto.LoginRequestDto;
import com.blibli.AuthService.dto.LoginResponseDto;
import com.blibli.AuthService.dto.RegisterRequestDto;
import com.blibli.AuthService.service.AuthService;
import com.blibli.AuthService.service.UserService;
import com.blibli.AuthService.util.ApiResponse;
import com.blibli.AuthService.util.ResponseUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
@SecurityRequirement(name = "bearerAuth")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;
//    private final JwtUtil jwtUtil;

    public AuthController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
//        this.jwtUtil = jwtUtil;
    }


    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@RequestBody RegisterRequestDto registerRequestDto) {
        userService.register(registerRequestDto);
        return ResponseUtil.created("User registered successfully", null);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@RequestBody LoginRequestDto loginRequestDto) {
        LoginResponseDto response = authService.login(loginRequestDto);
        return ResponseUtil.success("User is Logged in Successfully", response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().build();
        }
        String token = authorization.substring(7).trim();
        authService.revokeToken(token);
        return ResponseEntity.ok().build();
    }

//    @GetMapping("/check-revoked")
//    public ResponseEntity<Map<String, Object>> checkRevoked(@RequestParam("jti") String jti) {
//        boolean revoked = authService.isRevoked(jti);
//        return ResponseEntity.ok(Map.of("jti", jti, "revoked", revoked));
//    }

}
