package com.blublu.member.service;

import com.blublu.member.entity.Member;
import com.blublu.member.exception.UsernameNotExistException;
import com.blublu.member.interfaces.AuthenticationService;
import com.blublu.member.interfaces.MemberService;
import com.blublu.member.model.request.LoginRequest;
import com.blublu.member.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {
  @Autowired
  private AuthenticationManager authenticationManager;
  @Autowired
  private JwtUtil jwtUtil;
  @Autowired
  private MemberService memberService;
  @Autowired
  private CustomUserDetailsService customUserDetailsService;

  public UserDetails authenticateUser(LoginRequest request, String secret) {
    Member member = memberService.findByUsername(request.getUsername()).getFirst();
    if (Objects.isNull(member)) {
      throw new UsernameNotExistException("Username " + " ");
    }
    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(),
        request.getPassword()));

    UserDetails userDetails = customUserDetailsService.loadUserByUsername(request.getUsername());
    return userDetails;
  }
}
