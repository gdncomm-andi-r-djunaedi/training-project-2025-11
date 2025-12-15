package com.yuna.auth.service.impl;

import com.yuna.auth.client.MemberClient;
import com.yuna.auth.dto.RegisterUserDto;
import com.yuna.auth.model.User;
import com.yuna.auth.request.CreateMemberRequest;
import com.yuna.auth.response.MemberResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  @Autowired
  private MemberClient memberClient;

  public MemberResponse getMember() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    User currentUser = (User) authentication.getPrincipal();

    return memberClient.getMember(currentUser.getUsername());
  }

  public MemberResponse createMember(RegisterUserDto registerUserDto) {
    CreateMemberRequest createMemberRequest = CreateMemberRequest.builder()
        .username(registerUserDto.getUsername())
        .name(registerUserDto.getName())
        .email(registerUserDto.getEmail())
        .password(registerUserDto.getPassword())
        .build();

    return memberClient.createMember(createMemberRequest);
  }
}