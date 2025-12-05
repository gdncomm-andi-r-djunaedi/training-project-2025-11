package com.zasura.member.controller;

import com.zasura.member.dto.CommonResponse;
import com.zasura.member.dto.LoginRequest;
import com.zasura.member.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/member")
public class MemberController {
  @Autowired
  private MemberService memberService;

  @PostMapping("/_verify")
  public ResponseEntity<CommonResponse> verifyMember(@Valid @RequestBody LoginRequest request) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(CommonResponse.builder()
            .status(HttpStatus.OK.name())
            .code(HttpStatus.OK.value())
            .success(true)
            .data(memberService.verifyMember(request))
            .build());
  }

  @GetMapping("/{memberId}")
  public ResponseEntity<CommonResponse> getMember(@PathVariable UUID memberId) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(CommonResponse.builder()
            .status(HttpStatus.OK.name())
            .code(HttpStatus.OK.value())
            .success(true)
            .data(memberService.getMember(memberId))
            .build());
  }
}
