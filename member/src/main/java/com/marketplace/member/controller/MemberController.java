package com.marketplace.member.controller;

import com.marketplace.common.dto.ApiResponse;
import com.marketplace.common.exception.UnauthorizedException;
import com.marketplace.common.security.MemberContext;
import com.marketplace.common.security.MemberContextHolder;
import com.marketplace.member.dto.LoginRequest;
import com.marketplace.member.dto.LoginResponse;
import com.marketplace.member.dto.MemberResponse;
import com.marketplace.member.dto.RegisterRequest;
import com.marketplace.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "Member", description = "Member management APIs - registrasi, login, logout, dan profil member")
public class MemberController {

    private final MemberService memberService;

    @Operation(
            summary = "Registrasi member baru",
            description = "Membuat akun member baru dengan email dan password. Email harus unik."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Registrasi berhasil",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Email sudah terdaftar atau validasi gagal")
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<MemberResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        log.info("Register request received for email: {}", request.getEmail());
        MemberResponse response = memberService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Registration successful"));
    }

    @Operation(
            summary = "Login member",
            description = "Autentikasi member dengan email dan password. Mengembalikan JWT access token."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Login berhasil"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Email atau password salah")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        log.info("Login request received for email: {}", request.getEmail());
        LoginResponse response = memberService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    @Operation(
            summary = "Logout member",
            description = "Logout member dan invalidate JWT token. Token akan ditambahkan ke blacklist.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Logout berhasil")
    })
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        MemberContext context = MemberContextHolder.getContext();
        if (context == null || context.getToken() == null) {
            throw new UnauthorizedException("Not authenticated");
        }
        log.info("Logout request received for member: {}", context.getMemberId());
        memberService.logout(context.getToken());
        return ResponseEntity.ok(ApiResponse.success(null, "Logout successful"));
    }

    @Operation(
            summary = "Get profil member yang sedang login",
            description = "Mengambil informasi member berdasarkan JWT token",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Berhasil mendapatkan profil"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Token tidak valid"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Member tidak ditemukan")
    })
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberResponse>> getCurrentMember() {
        MemberContext context = MemberContextHolder.getContext();
        if (context == null) {
            throw new UnauthorizedException("Not authenticated");
        }
        log.info("Get current member request for id: {}", context.getMemberId());
        MemberResponse response = memberService.getMemberById(context.getMemberId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
            summary = "Get member by ID",
            description = "Mengambil informasi member berdasarkan ID (requires authentication)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Berhasil mendapatkan member"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Member tidak ditemukan")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MemberResponse>> getMemberById(
            @Parameter(description = "Member ID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id) {
        log.info("Get member by id request: {}", id);
        MemberResponse response = memberService.getMemberById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
