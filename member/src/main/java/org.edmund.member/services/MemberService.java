package org.edmund.member.services;

import org.edmund.member.dto.LoginMemberDto;
import org.edmund.member.dto.RegisterMemberDto;
import org.edmund.member.entity.Member;
import org.edmund.member.response.GetMemberResponse;
import org.edmund.member.response.LoginResponse;

import java.util.Optional;

public interface MemberService {
    Member registerMember(RegisterMemberDto request);
    LoginResponse loginMember(LoginMemberDto request);
    Optional<GetMemberResponse> findByEmail(String email);
}