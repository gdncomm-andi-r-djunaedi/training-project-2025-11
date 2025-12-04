package com.gdn.training.member.service;

import com.gdn.training.member.dto.LoginResponse;

public interface MemberService {

    void register(String username, String email, String password);

    LoginResponse login(String username, String password);

    void logout(String token);
}
