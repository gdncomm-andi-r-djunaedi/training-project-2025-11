package com.microservice.api_gateway.controller;

import com.microservice.api_gateway.client.LogInFeign;
import com.microservice.api_gateway.dto.LogInUserResponseDto;
import com.microservice.api_gateway.dto.LoginRequestDto;
import com.microservice.api_gateway.dto.LoginResponseDto;
import com.microservice.api_gateway.dto.MemberLogInResponseDto;
import com.microservice.api_gateway.service.JWTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/member")
public class AuthController {

    @Autowired
    private LogInFeign logInFeign;

    @Autowired
    private JWTService jwtService;

    @PostMapping("/logIn")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginRequest) {
        try {
            MemberLogInResponseDto memberResponse = logInFeign.logIn(loginRequest);

            // Check if member is valid
            if (memberResponse != null && Boolean.TRUE.equals(memberResponse.getIsMember()) && memberResponse.getUserId() != null) {
                // Generate JWT token with userId only
                String token = jwtService.generateToken(memberResponse.getUserId());

                // Return response with JWT token and userId
                LoginResponseDto response = new LoginResponseDto(
                        token,
                        memberResponse.getUserId(),
                        "Login successful"
                );

                LogInUserResponseDto logInUserResponseDto = new LogInUserResponseDto();
                logInUserResponseDto.setToken(response.getToken());
                logInUserResponseDto.setMessage(response.getMessage());
                return new ResponseEntity<>(logInUserResponseDto, HttpStatus.OK);
            } else {
                // Member not found or invalid
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid credentials or member not found");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Login failed: " + e.getMessage());
        }
    }
}