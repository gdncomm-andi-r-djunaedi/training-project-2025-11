package com.sc.memberservice.controller;

import com.sc.memberservice.dto.LoginDto;
import com.sc.memberservice.dto.RegisterDto;
import com.sc.memberservice.dto.ValidateTokenResponse;
import com.sc.memberservice.exception.InvalidTokenException;
import com.sc.memberservice.service.MemberService;
import com.sc.memberservice.utils.JwtUtil;
import com.sc.utilsservice.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@RequestBody RegisterDto registerDto) {
        ApiResponse<String> response = memberService.register(registerDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@RequestBody LoginDto loginDto) {
        Pair<String, ApiResponse<String>> responseWithToken = memberService.login(loginDto);
        String token = responseWithToken.a;
        ApiResponse<String> response = responseWithToken.b;
        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + token)
                .body(response);
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<ValidateTokenResponse>> validateToken(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");

        try {
            String subject = jwtUtil.extractSubject(token);
            String[] parts = subject.split(":");
            String memberId = parts[0];
            String email = parts[1];

            ValidateTokenResponse response = new ValidateTokenResponse(memberId, email);

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (InvalidTokenException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.failure("INVALID_TOKEN", "Token is invalid or expired"));
        }
    }

}
