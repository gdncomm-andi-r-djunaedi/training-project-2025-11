package com.gdn.training.apigateway.infrastructure.controller;

import com.gdn.training.apigateway.application.usecase.RegisterMemberUseCase;
import com.gdn.training.apigateway.application.usecase.model.RegisterResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

record RegisterRequest(String fullName, String email, String rawPassword, String phoneNumber) {
}

@RestController
@RequestMapping("/api/auth")
public class RegisterController {
    private final RegisterMemberUseCase registerMemberUseCase;

    public RegisterController(RegisterMemberUseCase registerMemberUseCase) {
        this.registerMemberUseCase = registerMemberUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResult> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(
                registerMemberUseCase.execute(request.fullName(), request.email(), request.rawPassword(),
                        request.phoneNumber()));
    }
}
