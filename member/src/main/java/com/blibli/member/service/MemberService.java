package com.blibli.member.service;

import com.blibli.member.dto.LoginRequest;
import com.blibli.member.dto.LoginResponse;
import com.blibli.member.dto.MemberResponse;
import com.blibli.member.dto.RegisterRequest;
import org.springframework.context.annotation.Primary;

import java.util.UUID;

public interface MemberService {

    public MemberResponse register(RegisterRequest request);
    public LoginResponse authenticate(LoginRequest request);
    public MemberResponse getMemberById(UUID id);
    public MemberResponse getMemberByEmail(String email);

}
