package com.blibi.member.controller;

import com.blibi.member.dto.GenericResponse;
import com.blibi.member.dto.MemberLoginRequest;
import com.blibi.member.dto.MemberRegisterRequest;
import com.blibi.member.dto.MemberResponse;
import com.blibi.member.service.MemberService;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/member")
@Builder
public class MemberController {
    private final MemberService memberService;

    @PostMapping("/register")
    public GenericResponse<MemberResponse> register(@RequestBody MemberRegisterRequest request) {
        log.info("User registration started");
        return GenericResponse.<MemberResponse>builder()
                .status("SUCCESS")
                .message("Member registered")
                .data(memberService.register(request))
                .build();
    }
    @PostMapping("/login")
    public GenericResponse<MemberResponse> login(@RequestBody MemberLoginRequest request) {
        log.info("User login initiated");
        return GenericResponse.<MemberResponse>builder()
                .status("SUCCESS")
                .message("Logged in successful")
                .data(memberService.login(request))
                .build();
    }

}
