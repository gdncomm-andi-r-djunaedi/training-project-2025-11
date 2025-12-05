package org.jovian.apigateway.controller;


import lombok.RequiredArgsConstructor;
import org.jovian.apigateway.service.JwtService;
import org.jovian.apigateway.service.RedisTokenBlacklistService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtService jwtService;
    private final RedisTokenBlacklistService blacklistService;

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            long expiration = jwtService.getExpirationInSeconds(token);
            blacklistService.blacklistToken(token, expiration);
        }
        return ResponseEntity.ok().build();
    }
}