package com.microservice.api_gateway.controller;

import com.microservice.api_gateway.client.LogInFeign;
import com.microservice.api_gateway.dto.LogInUserResponseDto;
import com.microservice.api_gateway.dto.LoginRequestDto;
import com.microservice.api_gateway.dto.LoginResponseDto;
import com.microservice.api_gateway.dto.MemberLogInResponseDto;
import com.microservice.api_gateway.dto.MemberServiceResponseWrapper;
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
            MemberServiceResponseWrapper wrapper = logInFeign.logIn(loginRequest);
            if (wrapper == null || wrapper.getData() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid credentials or member not found");
            }

            MemberLogInResponseDto memberResponse = wrapper.getData();

            if (Boolean.TRUE.equals(wrapper.getSuccess()) &&
                    Boolean.TRUE.equals(memberResponse.getIsMember()) &&
                    memberResponse.getUserId() != null) {

                String token = jwtService.generateToken(memberResponse.getUserId());

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
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid credentials or member not found");
            }
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Login failed: " + e.getMessage());
        }
    }
}