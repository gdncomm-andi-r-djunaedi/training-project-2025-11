package com.example.memberservice.controller;

import com.example.memberservice.dto.AuthDto;
import com.example.memberservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseCookie;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;

    @PostMapping("/register")
    public String addNewUser(@RequestBody @Valid AuthDto.RegisterRequest user) {
        return service.saveUser(user);
    }

    @PostMapping("/login")
    public AuthDto.AuthResponse getToken(@RequestBody AuthDto.LoginRequest authRequest, HttpServletResponse response) {
        AuthDto.MemberValidationResponse validationResponse = service.validateUser(authRequest);
        String token = service.generateToken(authRequest.getUsername(), validationResponse.getUserId());
        ResponseCookie cookie = ResponseCookie.from("jwt", token)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(30 * 60)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
        return new AuthDto.AuthResponse(token, validationResponse.getUserId(), validationResponse.getUsername());
    }

}
