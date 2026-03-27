package com.example.api_gateway.service;

import com.example.api_gateway.request.LoginRequest;
import com.example.api_gateway.response.MessageResponse;
import com.example.api_gateway.request.RegisterRequest;

public interface MemberService {

    MessageResponse register(RegisterRequest request);
    MessageResponse login(LoginRequest loginRequest);
    MessageResponse logout(String email);
}
