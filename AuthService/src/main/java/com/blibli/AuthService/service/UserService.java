package com.blibli.AuthService.service;

import com.blibli.AuthService.dto.RegisterRequestDto;
import com.blibli.AuthService.entity.UserEntity;

public interface UserService {
    UserEntity register(RegisterRequestDto registerRequestDto);
}
