package com.blibli.AuthService.service;

import com.blibli.AuthService.dto.LoginRequestDto;
import com.blibli.AuthService.dto.LoginResponseDto;

public interface AuthService {
    LoginResponseDto login(LoginRequestDto loginRequestDto);

    void revokeToken(String token);

//    boolean isRevoked(String jti);
}
