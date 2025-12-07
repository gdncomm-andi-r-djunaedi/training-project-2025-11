package com.blibli.apiGateway.controller;

import com.blibli.apiGateway.client.MemberClient;
import com.blibli.apiGateway.dto.*;
import com.blibli.apiGateway.services.impl.InMemoryTokenBlacklist;
import com.blibli.apiGateway.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private MemberClient memberClient;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private InMemoryTokenBlacklist inMemoryTokenBlacklist;


    @PostMapping("/register")
    public UserRegisterDTO registerMember(@Valid @RequestBody UserRegisterDTO userRegisterDTO) {
        return memberClient.register(userRegisterDTO);
    }


    @PostMapping("/login")
    public UserLoginResponseDTO login(@RequestBody UserLoginRequestDTO userLoginRequestDTO) {
        boolean valid = memberClient.validateMember(userLoginRequestDTO);
        if (!valid) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid username or password");
        }
        String token = jwtUtil.generateToken(userLoginRequestDTO.getEmailId());
        return new UserLoginResponseDTO(token, userLoginRequestDTO.getEmailId(),"LoggedIn successfully");
    }


    @GetMapping("/userProfile")
    public ResponseEntity<?> getUserProfile(Authentication authentication) {
        String emailId = authentication.getName();
        return memberClient.getUserProfile("Bearer " + jwtUtil.generateToken(emailId));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader(value = "Authorization",required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Invalid token");
        }
        String token = authHeader.substring(7);
        long expiry = jwtUtil.getExpirationFromToken(token);
        inMemoryTokenBlacklist.blacklistToken(token,expiry);

        return ResponseEntity.ok("Logged out successfully");
    }

}
