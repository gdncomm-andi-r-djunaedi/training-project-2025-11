package com.zasura.member.controller;

import com.zasura.member.dto.CommonResponse;
import com.zasura.member.dto.CreateMemberRequest;
import com.zasura.member.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  @Autowired
  private MemberService memberService;

  @PostMapping("/register")
  public ResponseEntity<CommonResponse> createMember(@Valid @RequestBody CreateMemberRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(CommonResponse.builder()
            .status(HttpStatus.CREATED.name())
            .code(HttpStatus.CREATED.value())
            .success(memberService.registerMember(request))
            .build());
  }
}
