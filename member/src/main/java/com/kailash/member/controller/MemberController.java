package com.kailash.member.controller;

import com.kailash.member.dto.*;
import com.kailash.member.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping
public class MemberController {

    @Autowired
    MemberService svc;

    @PostMapping("/auth/register")
    public ResponseEntity<ApiResponse<MemberResponse>> register(@RequestBody RegisterRequest req) {
        ApiResponse<MemberResponse> resp = svc.register(req);
        return ResponseEntity.created(URI.create("/members/" + resp.getData().getId())).body(resp);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<MemberResponse>> login(@RequestBody LoginRequest req) {
        ApiResponse<MemberResponse> resp = svc.login(req);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/members/{id}")
    public ResponseEntity<ApiResponse<MemberResponse>> get(@PathVariable("id") String id) {
        ApiResponse<MemberResponse> resp = svc.getById(UUID.fromString(id));
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/members/me")
    public ResponseEntity<ApiResponse<MemberResponse>> me(@RequestHeader(value = "X-User-Id", required = false) String userId) {
        if (userId == null) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(null, false, "Missing X-User-Id header"));
        }
        ApiResponse<MemberResponse> resp = svc.getById(UUID.fromString(userId));
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/members/{id}")
    public ResponseEntity<ApiResponse<MemberResponse>> update(@PathVariable("id") String id, @RequestBody RegisterRequest req) {
        ApiResponse<MemberResponse> resp = svc.update(UUID.fromString(id), req);
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/members/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") String id) {
        ApiResponse<Void> resp = svc.delete(UUID.fromString(id));
        return ResponseEntity.ok(resp);
    }
}

