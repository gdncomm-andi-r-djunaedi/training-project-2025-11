package com.dev.onlineMarketplace.MemberService.service;

import com.dev.onlineMarketplace.MemberService.dto.*;

public interface MemberService {

    MemberDTO register(RegisterRequestDTO request);

    LoginResponseDTO login(LoginRequestDTO request);

    void logout(String token);

}
