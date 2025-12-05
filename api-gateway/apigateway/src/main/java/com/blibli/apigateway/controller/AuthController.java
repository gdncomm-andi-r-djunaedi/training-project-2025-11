package com.blibli.apigateway.controller;

import com.blibli.apigateway.client.MemberClient;
import com.blibli.apigateway.dto.request.LoginRequest;
import com.blibli.apigateway.dto.response.LoginResponse;
import com.blibli.apigateway.dto.response.LogoutResponse;
import com.blibli.apigateway.dto.request.MemberDto;
import com.blibli.apigateway.service.AuthService;
import com.blibli.apigateway.service.JwtService;
import com.blibli.apigateway.service.TokenBlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication API for member login and token generation")
public class AuthController {
    
    private final AuthService authService;
    private final MemberClient memberClient;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    
    public AuthController(AuthService authService, MemberClient memberClient, JwtService jwtService, TokenBlacklistService tokenBlacklistService) {
        this.authService = authService;
        this.memberClient = memberClient;
        this.jwtService = jwtService;
        this.tokenBlacklistService = tokenBlacklistService;
    }
    
    private String extractTokenFromHeader(String authHeader) {
        if (authHeader == null || authHeader.trim().isEmpty()) {
            throw new RuntimeException("Missing Authorization header. Please include: Authorization: Bearer <token>");
        }
        
        authHeader = authHeader.trim();
        
        if (!authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid Authorization header format. Expected: Bearer <token>");
        }
        
        String token = authHeader.substring(7).trim();
        if (token.isEmpty()) {
            throw new RuntimeException("Token is empty after Bearer prefix");
        }

        boolean isValid = jwtService.validateToken(token);
        if (!isValid) {
            log.error("Token validation failed - validateToken returned false");
            throw new RuntimeException("Invalid or expired token");
        }
        
        log.info("Token validated successfully");
        return token;
    }
    
    @PostMapping("/login")
    @Operation(summary = "Member Login", description = "Authenticate member credentials and generate JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class)))
    })
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        LoginResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/profile")
    @Operation(summary = "Get member profile", description = "Get member details using JWT token",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully",
                    content = @Content(schema = @Schema(implementation = MemberDto.class))),
            @ApiResponse(responseCode = "401", description = "Invalid or expired token",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Member not found",
                    content = @Content)
    })
    public ResponseEntity<?> getProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader == null || authHeader.trim().isEmpty()) {
            log.warn("Missing Authorization header");
            throw new RuntimeException("Missing Authorization header. Please include: Authorization: Bearer <token>");
        }

        extractTokenFromHeader(authHeader);
        log.info("Token validated successfully, calling member service to get profile");
        
        MemberDto member = memberClient.getMemberDetails(authHeader);
        log.info("Profile retrieved successfully for email: {}", member != null ? member.getEmail() : "null");
        
        return ResponseEntity.ok(member);
    }
    
    @PostMapping("/logout")
    @Operation(summary = "Member Logout", description = "Logout member and invalidate session",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout successful",
                    content = @Content(schema = @Schema(implementation = LogoutResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid or expired token",
                    content = @Content)
    })
    public ResponseEntity<?> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        log.info("Logout request - Authorization header present: {}", authHeader != null);
        
        if (authHeader == null || authHeader.trim().isEmpty()) {
            log.warn("Missing Authorization header for logout");
            throw new RuntimeException("Missing Authorization header. Please include: Authorization: Bearer <token>");
        }
        
        String token = extractTokenFromHeader(authHeader);
        log.info("Token validated successfully for logout");
        
        try {
            String email = JwtService.extractEmail(token);
            log.info("User {} logged out successfully", email);
        } catch (Exception emailExtractError) {
            log.warn("Could not extract email from token for logging: {}", emailExtractError.getMessage());
        }
        
        tokenBlacklistService.blacklistToken(token);
        log.info("Token has been blacklisted and is now invalid");
        
        LogoutResponse response = new LogoutResponse("Logout successful. Token has been invalidated.", "SUCCESS");
        return ResponseEntity.ok(response);
    }

}

