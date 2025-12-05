package com.zasura.member.service;

import com.zasura.member.dto.CreateMemberRequest;
import com.zasura.member.dto.LoginRequest;
import com.zasura.member.dto.LoginResponse;
import com.zasura.member.dto.MemberDetailResponse;
import com.zasura.member.entity.Member;

import java.util.UUID;

public interface MemberService {
  boolean registerMember(CreateMemberRequest createMemberRequest);

  LoginResponse verifyMember(LoginRequest request);

  Member getMember(UUID memberId);
}
