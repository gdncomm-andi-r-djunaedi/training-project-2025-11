package com.microservice.member.service;

import com.microservice.member.dto.LoginRequestDto;
import com.microservice.member.dto.MemberLogInResponseDto;
import com.microservice.member.dto.RegisterRequestDto;
import com.microservice.member.dto.RegisterResponseDto;
import org.springframework.http.HttpStatusCode;

public interface MemberService {
    RegisterResponseDto registerNewUser(RegisterRequestDto registerRequestDto);

    MemberLogInResponseDto validateUser(LoginRequestDto loginRequestDto);
}
