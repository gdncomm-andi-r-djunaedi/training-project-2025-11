package com.blublu.member.controller;

import com.blublu.member.interfaces.AuthenticationService;
import com.blublu.member.model.request.LoginRequest;
import com.blublu.member.model.request.SignUpRequest;
import com.blublu.member.model.response.GenericBodyResponse;
import com.blublu.member.model.response.LoginResponse;
import com.blublu.member.service.MemberServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
@RequestMapping("/member")
public class MemberController {

  @Autowired
  MemberServiceImpl memberService;

  @Autowired
  AuthenticationService authenticationService;

  @RequestMapping(path = "/login", method = RequestMethod.POST)
  public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
    String username = authenticationService.authenticateUser(loginRequest).getUsername();
    return ResponseEntity.ok(LoginResponse.builder().username(username).success(true).build());
  }

  @RequestMapping(path = "/sign-up", method = RequestMethod.POST)
  public ResponseEntity<?> signUp(@RequestBody SignUpRequest signUpRequest) {
    memberService.signUp(signUpRequest);
    return ResponseEntity.ok().body(GenericBodyResponse.builder().content(new ArrayList<>()).success(true).build());
  }

  @RequestMapping(path = "/logout", method = RequestMethod.POST)
  public ResponseEntity<?> logout() {
    return ResponseEntity.ok(GenericBodyResponse.builder().success(true).build());
  }

}
