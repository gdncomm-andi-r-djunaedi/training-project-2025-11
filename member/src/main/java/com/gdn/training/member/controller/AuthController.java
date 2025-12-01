package com.gdn.training.member.controller;

import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gdn.training.member.auth.JwtUtil;
import com.gdn.training.member.model.request.LoginRequest;
import com.gdn.training.member.model.response.ErrorResponse;
import com.gdn.training.member.model.response.LoginResponse;
import com.gdn.training.member.model.User;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            String email = authentication.getName();
            User user = User.builder()
                    .email(email)
                    .password("")
                    .name("")
                    .build();

            String jwt = jwtUtil.createToken(user);
            return ResponseEntity.ok(LoginResponse.builder()
                    .email(email)
                    .token(jwt)
                    .build());

        } catch (BadCredentialsException e) {
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .message("Invalid email or password")
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            // } catch (Exception e) {
            // ErrorResponse errorResponse = ErrorResponse.builder()
            // .message("Internal server error")
            // .build();
            // return
            // ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
