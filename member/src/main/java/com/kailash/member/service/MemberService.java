package com.kailash.member.service;

import com.kailash.member.dto.*;
import com.kailash.member.entity.Member;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;
@Service
public interface MemberService {
    ApiResponse<MemberResponse> register(RegisterRequest req);
    ApiResponse<MemberResponse> login(LoginRequest req);
    ApiResponse<MemberResponse> getById(UUID id);
    ApiResponse<MemberResponse> update(UUID id, RegisterRequest req);
    ApiResponse<Void> delete(UUID id);
}
