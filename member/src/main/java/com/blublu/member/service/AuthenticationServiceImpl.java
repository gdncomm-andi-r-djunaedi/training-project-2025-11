package com.blublu.member.service;

import com.blublu.member.entity.Member;
import com.blublu.member.exception.UsernameNotExistException;
import com.blublu.member.exception.WrongPasswordException;
import com.blublu.member.interfaces.AuthenticationService;
import com.blublu.member.interfaces.MemberService;
import com.blublu.member.model.request.LoginRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class AuthenticationServiceImpl implements AuthenticationService {
  @Autowired
  private AuthenticationManager authenticationManager;
  @Autowired
  private MemberService memberService;
  @Autowired
  private CustomUserDetailsService customUserDetailsService;

  public UserDetails authenticateUser(LoginRequest request) {
    if (!memberService.isUsernameExist(request.getUsername())) {
      throw new UsernameNotExistException("Username: " + request.getUsername() + " does not exist");
    }

    try {
      authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(),
          request.getPassword()));
    } catch (RuntimeException runtimeException) {
      throw new WrongPasswordException("Wrong Password!");
    }

    return customUserDetailsService.loadUserByUsername(request.getUsername());
  }
}
