package com.marketplace.member.controller;

import com.marketplace.common.controller.BaseCommandController;
import com.marketplace.common.dto.ApiResponse;
import com.marketplace.common.dto.UserDetailsResponse;
import com.marketplace.common.dto.ValidateCredentialsRequest;
import com.marketplace.member.command.RegisterMemberCommand;
import com.marketplace.member.command.ValidateCredentialsCommand;
import com.marketplace.member.dto.MemberResponse;
import com.marketplace.member.dto.RegisterRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/member")
public class MemberController extends BaseCommandController {

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<MemberResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request received for email: {}", request.getEmail());
        MemberResponse response = execute(RegisterMemberCommand.class, request);
        return createdResponse("User registered successfully", response);
    }

    /**
     * Validate credentials endpoint (for internal use by API Gateway).
     */
    @PostMapping("/validate-credentials")
    public ResponseEntity<ApiResponse<UserDetailsResponse>> validateCredentials(
            @Valid @RequestBody ValidateCredentialsRequest request) {
        log.info("Credential validation request for email: {}", request.getEmail());
        UserDetailsResponse response = execute(ValidateCredentialsCommand.class, request);
        return okResponse("Credentials validated", response);
    }
}
