package com.ecom.member.Service;

import com.ecom.member.Dto.MemberDto;

public interface MemberService {

    boolean register(MemberDto memberDto);

    String login(String email, String password);

    String logout(String userId);

}
