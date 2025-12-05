package com.gdn.training.member.service;

import com.gdn.training.member.dto.LoginResponse;
import com.gdn.training.member.dto.LogoutResponse;
import com.gdn.training.member.dto.RegisterResponse;

public interface MemberService {

    RegisterResponse register(String username, String email, String password);

    LoginResponse login(String username, String password);

    LogoutResponse logout(String authorizationHeader);
}
