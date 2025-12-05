package com.blublu.member.service;

import com.blublu.member.entity.Member;
import com.blublu.member.exception.UsernameExistException;
import com.blublu.member.interfaces.MemberService;
import com.blublu.member.model.request.SignUpRequest;
import com.blublu.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class MemberServiceImpl implements MemberService {

  @Autowired
  MemberRepository memberRepository;

  @Autowired
  PasswordEncoder passwordEncoder;

  public boolean isUsernameExist(String username) {
    return memberRepository.countByUsername(username) > 0;
  }

  public void signUp(SignUpRequest signUpRequest) {
    if (isUsernameExist(signUpRequest.getUsername())) {
      throw new UsernameExistException(
          "Username with name " + signUpRequest.getUsername() + " exist! Please login instead");
    }

    memberRepository.save(Member.builder()
        .id(null)
        .username(signUpRequest.getUsername())
        .password(passwordEncoder.encode(signUpRequest.getPassword()))
        .build());
  }
}
