package com.yuna.auth.service.impl;

import com.yuna.auth.client.MemberClient;
import com.yuna.auth.dto.LoginUserDto;
import com.yuna.auth.model.User;
import com.yuna.auth.response.MemberResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class AuthenticationService {
  @Autowired
  private TokenServiceImpl tokenServiceImpl;

  @Autowired
  private MemberClient memberClient;

  private final SecurityContextLogoutHandler securityContextLogoutHandler = new SecurityContextLogoutHandler();

  public String authenticate(LoginUserDto input) {
    MemberResponse memberResponse = memberClient.getMemberByUsernameAndPassword(input);
    User user = User.builder().username(memberResponse.getUsername()).build();
    String jwt = tokenServiceImpl.generateToken(user);
    tokenServiceImpl.saveToken(jwt);
    return jwt;
  }

  public Boolean logout(HttpServletRequest request, HttpServletResponse response) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.isAuthenticated()) {
      securityContextLogoutHandler.logout(request, response, authentication);
      final String authHeader = request.getHeader("Authorization");
      String jwt;
      if  (authHeader != null && authHeader.startsWith("Bearer ")) {
        jwt = authHeader.substring(7);
      } else {
        final Cookie authCookie = Arrays.stream(request.getCookies()).filter(cookie -> cookie.getName().equals("token")).findFirst().get();
        jwt = authCookie.getValue();
      }
      tokenServiceImpl.invalidateToken(jwt);
      SecurityContextHolder.clearContext();
      return true;
    } else {
      throw new RuntimeException("User doesn't exist");
    }
  }
}
