package com.example.member.service;

import com.example.member.dto.*;

public interface UserService {

    MessageResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
