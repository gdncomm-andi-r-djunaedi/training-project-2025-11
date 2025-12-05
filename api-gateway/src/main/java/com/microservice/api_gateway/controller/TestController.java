package com.microservice.api_gateway.controller;

import com.microservice.api_gateway.service.JWTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private JWTService jwtService;

    @GetMapping("/token")
    public ResponseEntity<?> testToken(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> response = new HashMap<>();

        if (authHeader == null) {
            response.put("error", "No Authorization header provided");
            response.put("receivedHeaders", "Authorization header is null");
            return ResponseEntity.badRequest().body(response);
        }

        if (!authHeader.startsWith("Bearer ")) {
            response.put("error", "Authorization header must start with 'Bearer '");
            response.put("receivedHeader", authHeader);
            return ResponseEntity.badRequest().body(response);
        }

        String token = authHeader.substring(7);
        try {
            Long userId = jwtService.extractUserId(token);
            boolean expired = jwtService.isTokenExpired(token);
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            response.put("tokenValid", userId != null);
            response.put("userId", userId);
            response.put("expired", expired);
            response.put("authenticationSet", auth != null);
            response.put("authPrincipal", auth != null ? auth.getPrincipal() : null);
            response.put("tokenLength", token.length());
            response.put("tokenPreview", token.substring(0, Math.min(50, token.length())) + "...");

            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            response.put("error", "Error processing token");
            response.put("message", e.getMessage());
            response.put("exception", e.getClass().getSimpleName());
            return ResponseEntity.badRequest().body(response);
        }
    }
}