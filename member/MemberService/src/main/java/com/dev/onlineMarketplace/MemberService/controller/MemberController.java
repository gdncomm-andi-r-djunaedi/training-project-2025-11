package com.dev.onlineMarketplace.MemberService.controller;

import com.dev.onlineMarketplace.MemberService.dto.*;
import com.dev.onlineMarketplace.MemberService.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/member")
@Tag(name = "Member", description = "APIs for member registration, login and logout")
public class MemberController {

    private static final Logger logger = LoggerFactory.getLogger(MemberController.class);

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new member", description = "Create a new member account with email and password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully", content = @Content(schema = @Schema(implementation = GDNResponseData.class))),
            @ApiResponse(responseCode = "409", description = "User already registered"),
            @ApiResponse(responseCode = "400", description = "Validation failed")
    })
    public ResponseEntity<GDNResponseData<MemberDTO>> register(@Valid @RequestBody RegisterRequestDTO request) {
        logger.info("POST /api/v1/member/register - Email: {}", request.getEmail());

        MemberDTO memberDTO = memberService.register(request);

        logger.info("Registration successful for email: {}", request.getEmail());
        return ResponseEntity.ok(GDNResponseData.success(memberDTO, "User registered successfully"));
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate user and return JWT tokens")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful", content = @Content(schema = @Schema(implementation = GDNResponseData.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<GDNResponseData<LoginResponseDTO>> login(@Valid @RequestBody LoginRequestDTO request) {
        logger.info("POST /api/v1/member/login - Username: {}", request.getUsername());

        LoginResponseDTO loginResponse = memberService.login(request);

        logger.info("Login successful for username: {}", request.getUsername());
        return ResponseEntity.ok(GDNResponseData.success(loginResponse, "Login successful"));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Logout the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logged out successfully")
    })
    public ResponseEntity<GDNResponseData<String>> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        logger.info("POST /api/v1/member/logout");

        memberService.logout(authHeader);

        logger.info("Logout successful");
        return ResponseEntity.ok(GDNResponseData.success("Logged out successfully", "Logged out successfully"));
    }

}
