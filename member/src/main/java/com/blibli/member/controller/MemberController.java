package com.blibli.member.controller;




import com.blibli.member.dto.ApiResponse;
import com.blibli.member.dto.LoginRequest;
import com.blibli.member.dto.LoginResponse;
import com.blibli.member.dto.MemberResponse;
import com.blibli.member.dto.RegisterRequest;
import com.blibli.member.exception.BadRequestException;
import com.blibli.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<MemberResponse>> register(
            @Valid @RequestBody RegisterRequest request) throws BadRequestException {
        MemberResponse response = memberService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        LoginResponse response = memberService.authenticate(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MemberResponse>> getMember(
            @PathVariable String id) {
        MemberResponse response = memberService.getMemberById(UUID.fromString(id));
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
