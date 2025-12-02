package com.example.member.service;

import com.example.member.dto.UserRequestDto;
import com.example.member.dto.UserResponseDTO;

import com.example.member.dto.LoginRequestDto;

public interface UserService {

    UserResponseDTO registerUser(UserRequestDto userRequestDto);
    String loginUser(LoginRequestDto loginRequestDto);
}
