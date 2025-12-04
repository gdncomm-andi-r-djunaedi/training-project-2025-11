package com.marketplace.member.controller;

import com.marketplace.common.dto.ApiResponse;
import com.marketplace.common.dto.UserDetailsResponse;
import com.marketplace.common.dto.ValidateCredentialsRequest;
import com.marketplace.member.dto.MemberResponse;
import com.marketplace.member.dto.RegisterRequest;
import com.marketplace.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<MemberResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request received for username: {}", request.getUsername());
        MemberResponse response = memberService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", response));
    }

    /**
     * Validate credentials endpoint (for internal use by API Gateway)
     */
    @PostMapping("/validate-credentials")
    public ResponseEntity<ApiResponse<UserDetailsResponse>> validateCredentials(
            @Valid @RequestBody ValidateCredentialsRequest request) {
        log.info("Credential validation request for username: {}", request.getUsername());
        UserDetailsResponse response = memberService.validateCredentials(request);
        return ResponseEntity.ok(ApiResponse.success("Credentials validated", response));
    }
}
