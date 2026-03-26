package com.ApiGateway.GateWay.controller;

import com.ApiGateway.GateWay.auth.ActiveTokenStore;
import com.ApiGateway.GateWay.config.JwtUtil;
import com.ApiGateway.GateWay.dto.LoginRequestDTO;
import com.ApiGateway.GateWay.dto.LoginResponse;
import com.ApiGateway.GateWay.dto.UserDTO;
import com.ApiGateway.GateWay.exception.AuthException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    ActiveTokenStore tokenStore;

    private final String memberServiceUrl = "http://localhost:8083/member/login";

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO request) {
        try {
            UserDTO user = restTemplate.postForObject(memberServiceUrl, request, UserDTO.class);
            if (user == null || user.getUsername() == null) {
                throw new AuthException("Authentication failed: Invalid credentials.", HttpStatus.UNAUTHORIZED);
            }
            String existingToken = tokenStore.getToken(user.getUsername());
            if (existingToken != null && jwtUtil.isTokenValid(existingToken)) {
                return ResponseEntity.ok(new LoginResponse(existingToken));
            }

            String token = jwtUtil.generateToken(user.getUsername());
            tokenStore.saveToken(user.getUsername(), token, jwtUtil.getExpirationTime(token));

            return ResponseEntity.ok(new LoginResponse(token));

        } catch (Exception e) {
            throw new AuthException("Authentication failed: Invalid credentials.", HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AuthException("Missing or invalid Authorization header", HttpStatus.BAD_REQUEST);
        }
        String token = authHeader.replace("Bearer ", "").trim();
        String username;
        try {
            username = jwtUtil.extractUsername(token);
        } catch (Exception e) {
            throw new AuthException("Invalid Token", HttpStatus.UNAUTHORIZED);
        }
        String storedToken = tokenStore.getToken(username);
        if (storedToken == null || !storedToken.equals(token)) {
            throw new AuthException("Token already invalidated or not found", HttpStatus.UNAUTHORIZED);
        }
        tokenStore.removeToken(username);
        return ResponseEntity.ok("Logged out successfully");
    }

}
