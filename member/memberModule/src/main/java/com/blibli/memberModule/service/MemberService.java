package com.blibli.memberModule.service;

import com.blibli.memberModule.dto.LoginRequestDto;
import com.blibli.memberModule.dto.LoginResponseDto;
import com.blibli.memberModule.dto.MemberRequestDto;
import com.blibli.memberModule.dto.MemberResponseDto;
import jakarta.validation.Valid;

public interface MemberService {

    MemberResponseDto register(MemberRequestDto request);

    LoginResponseDto login(@Valid LoginRequestDto request);

    void logout(Long memberId);
}
