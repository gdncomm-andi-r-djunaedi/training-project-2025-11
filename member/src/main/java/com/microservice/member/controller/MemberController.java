package com.microservice.member.controller;

import com.microservice.member.dto.LoginRequestDto;
import com.microservice.member.dto.MemberLogInResponseDto;
import com.microservice.member.dto.RegisterRequestDto;
import com.microservice.member.dto.RegisterResponseDto;
import com.microservice.member.service.MemberService;
import com.microservice.member.wrapper.ApiResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/member")
public class MemberController {

    @Autowired
    MemberService memberService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponseDto>> registerUser(@Valid @RequestBody RegisterRequestDto registerRequestDto){
        log.info("Registration request for email: {}", registerRequestDto.getEmail());

        RegisterResponseDto registerResponse = memberService.registerNewUser(registerRequestDto);
        ApiResponse<RegisterResponseDto> response = ApiResponse.success(registerResponse, HttpStatus.CREATED);

        log.info("User registered successfully with ID: {}", registerResponse.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/logIn")
    public ResponseEntity<ApiResponse<MemberLogInResponseDto>> logInUser(@Valid @RequestBody LoginRequestDto loginRequestDto){
        log.info("Login request for email: {}", loginRequestDto.getEmail());

        MemberLogInResponseDto loginResponse = memberService.validateUser(loginRequestDto);
        ApiResponse<MemberLogInResponseDto> response = ApiResponse.success(loginResponse, HttpStatus.OK);

        log.info("User logged in successfully with ID: {}", loginResponse.getUserId());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}