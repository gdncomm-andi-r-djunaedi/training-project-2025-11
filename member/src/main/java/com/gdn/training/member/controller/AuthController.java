package com.gdn.training.member.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gdn.training.common.model.BaseResponse;
import com.gdn.training.member.dto.LoginRequest;
import com.gdn.training.member.dto.RegisterRequest;
import com.gdn.training.member.dto.UserInfoResponse;
import com.gdn.training.member.service.MemberService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/internal/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "User authentication endpoints (internal use by Gateway)")
public class AuthController {

    private final MemberService memberService;

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Register a new user account")
    public ResponseEntity<BaseResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Register request received for {}", request.getEmail());
        memberService.register(request);
        return ResponseEntity.ok(BaseResponse.success("User registered successfully", null));
    }

    @PostMapping("/validate-credentials")
    @Operation(summary = "Validate credentials", description = "Validates user credentials and returns user info (called by Gateway)")
    public ResponseEntity<BaseResponse<UserInfoResponse>> validateCredentials(@Valid @RequestBody LoginRequest request) {
        log.debug("Validating credentials for {}", request.getEmail());
        UserInfoResponse userInfo = memberService.validateCredentials(request);
        log.info("Credentials validated for {}", userInfo.getEmail());
        return ResponseEntity.ok(BaseResponse.success("Credentials validated", userInfo));
    }
}