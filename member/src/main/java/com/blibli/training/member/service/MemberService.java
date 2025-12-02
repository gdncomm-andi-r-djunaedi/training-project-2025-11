package com.blibli.training.member.service;

import com.blibli.training.member.dto.LoginRequest;
import com.blibli.training.member.dto.MemberResponse;
import com.blibli.training.member.dto.RegisterRequest;

public interface MemberService {
    MemberResponse register(RegisterRequest request);
    MemberResponse login(LoginRequest request);
    java.util.List<MemberResponse> findAll();
    void logout(String token);
    MemberResponse findByUsername(String username);
}
