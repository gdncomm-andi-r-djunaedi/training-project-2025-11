package com.yuna.auth.controller;

import com.yuna.auth.response.MemberResponse;
import com.yuna.auth.service.impl.AuthenticationService;
import com.yuna.auth.service.impl.JwtService;
import com.yuna.auth.response.LoginResponse;
import com.yuna.auth.dto.LoginUserDto;
import com.yuna.auth.dto.RegisterUserDto;
import com.yuna.auth.service.impl.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/auth")
@RestController
public class AuthenticationController {

  @Autowired
  private JwtService jwtService;

  @Autowired
  private AuthenticationService authenticationService;

  @Autowired
  private UserService userService;

  @PostMapping("/signup")
  public ResponseEntity<MemberResponse> register(@RequestBody RegisterUserDto registerUserDto) {
    MemberResponse memberResponse = userService.createMember(registerUserDto);
    return new ResponseEntity<>(memberResponse, HttpStatus.CREATED);
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginUserDto loginUserDto, HttpServletResponse response) {
    String jwtToken = authenticationService.authenticate(loginUserDto);

    LoginResponse loginResponse = LoginResponse.builder()
        .token(jwtToken)
        .expiresIn(jwtService.getExpirationTime())
        .build();

    Cookie cookie = new Cookie("token", jwtToken);
    cookie.setHttpOnly(true);
    cookie.setMaxAge(0);
    cookie.setSecure(true);

    response.addCookie(cookie);

    return ResponseEntity.ok(loginResponse);
  }

  @PostMapping("/logout")
  public ResponseEntity<Boolean> logout(HttpServletRequest request,
      HttpServletResponse response) {
    return ResponseEntity.ok(authenticationService.logout(request, response));
  }
}
