package com.gdn.faurihakim.gateway.controller;

import com.gdn.faurihakim.gateway.service.AuthService;
import com.gdn.faurihakim.gateway.model.AuthRequest;
import com.gdn.faurihakim.gateway.model.AuthResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponse>> login(@RequestBody AuthRequest request) {
        return authService.login(request)
                .map(token -> ResponseEntity.ok(new AuthResponse(token)))
                .defaultIfEmpty(ResponseEntity.status(401).build());
    }
}
