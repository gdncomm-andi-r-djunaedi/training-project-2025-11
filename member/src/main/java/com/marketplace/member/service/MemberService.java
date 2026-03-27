package com.marketplace.member.service;

import com.marketplace.member.dto.LoginRequest;
import com.marketplace.member.dto.MemberResponse;
import com.marketplace.member.dto.RegisterRequest;

public interface MemberService {
    MemberResponse register(RegisterRequest request);
    MemberResponse login(LoginRequest request);
}
