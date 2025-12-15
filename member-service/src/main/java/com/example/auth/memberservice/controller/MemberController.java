package com.example.auth.memberservice.controller;

import com.example.auth.memberservice.dto.MemberRegisterRequest;
import com.example.auth.memberservice.dto.MemberRegisterResponse;
import com.example.auth.memberservice.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {

  private final MemberService memberService;

  @GetMapping("/users")
  public ResponseEntity<Page<MemberRegisterResponse>> getAllUsers(
          @RequestHeader("X-User-Id") String userId,
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "10") int size) {
    log.info("Get all members for user id {}", userId);
    Pageable pageable = PageRequest.of(page, size);
    return ResponseEntity.ok(memberService.getAllUsers(pageable));
  }
}
