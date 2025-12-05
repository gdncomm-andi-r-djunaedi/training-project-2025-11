package com.elfrida.member.service;

import com.elfrida.member.dto.LoginRequest;
import com.elfrida.member.dto.LoginResponse;
import com.elfrida.member.dto.MemberRequest;
import com.elfrida.member.model.Member;

public interface MemberService {

    Member register(MemberRequest request);

    LoginResponse login(LoginRequest request);

    void logout(String token);
}
