package com.blibli.member.service;

import com.blibli.member.dto.*;

public interface UserRegisterService {
    UserRegisterResponseDTO registerUser(UserRegisterRequestDTO userRegisterRequestDTO);

    LoginResponseDTO login(LoginRequestDTO loginRequestDTO);

    String getUserNameFromToken(String token);

    Boolean validateToken(String token, String userEmail);

    LogoutResponseDTO logout(String userEmail, String token);
}
