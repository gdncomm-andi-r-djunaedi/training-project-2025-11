package com.blibli.apigateway.service;

import com.blibli.apigateway.client.MemberClient;
import com.blibli.apigateway.dto.response.CheckUserResponse;
import com.blibli.apigateway.dto.request.LoginRequest;
import com.blibli.apigateway.dto.response.LoginResponse;
import com.blibli.apigateway.dto.request.ValidateMemberRequest;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthService {
    private final MemberClient memberClient;
    private final JwtService jwtService;
    
    public AuthService(MemberClient memberClient, JwtService jwtService) {
        this.memberClient = memberClient;
        this.jwtService = jwtService;
    }
    
    public LoginResponse login(LoginRequest loginRequest) {
        try {
            log.info("Attempting login for email: {}", loginRequest.getEmail());
            
            ValidateMemberRequest validateRequest = new ValidateMemberRequest(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
            );
            
            CheckUserResponse checkUserResponse = memberClient.validateMember(validateRequest);
            
            if (checkUserResponse == null) {
                log.warn("Member service returned null response for email: {}", loginRequest.getEmail());
                throw new RuntimeException("Invalid credentials");
            }
            
            String status = checkUserResponse.getStatus();
            if (status == null || !status.equals("VALID")) {
                String errorMessage = checkUserResponse.getMessage() != null 
                        ? checkUserResponse.getMessage() 
                        : "Invalid credentials";
                log.warn("Login failed for email: {}. Status: {}, Message: {}", 
                        loginRequest.getEmail(), status, errorMessage);
                throw new RuntimeException(errorMessage);
            }
            
            String email = checkUserResponse.getEmail();
            if (email == null || email.trim().isEmpty()) {
                log.warn("Member service returned null or empty email for login request");
                throw new RuntimeException("Invalid credentials");
            }
            
            log.debug("Credentials validated successfully for email: {}, generating token", email);
            String token = jwtService.generateToken(email);
            log.info("Login successful for email: {}", email);
            
            return new LoginResponse(
                    token,
                    "Login successful",
                    checkUserResponse.getEmail()
            );
        } catch (FeignException e) {
            log.error("Feign exception during login for email: {}. Status: {}, Message: {}", 
                    loginRequest.getEmail(), e.status(), e.getMessage(), e);
            throw new RuntimeException("Authentication failed: " + e.getMessage(), e);
        }
    }
}

