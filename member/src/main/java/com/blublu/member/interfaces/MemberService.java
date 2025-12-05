package com.blublu.member.interfaces;

import com.blublu.member.entity.Member;
import com.blublu.member.model.request.LoginRequest;
import com.blublu.member.model.request.SignUpRequest;

import java.util.List;

public interface MemberService {
  boolean isUsernameExist(String username);
  void signUp(SignUpRequest signUpRequest);
}
