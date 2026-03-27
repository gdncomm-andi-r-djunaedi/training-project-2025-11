package com.ecom.member.Controller;

import com.ecom.member.Dto.ApiResponse;
import com.ecom.member.Dto.LoginRequestDto;
import com.ecom.member.Dto.MemberDto;
import com.ecom.member.Service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/member")
public class MemberController {

    @Autowired
    MemberService memberService;

    @PostMapping("/register")
    public ApiResponse register(@RequestBody MemberDto memberDto) {
        memberService.register(memberDto);
        return ApiResponse.success(201,"Registration Successful", "[]");
    }

    @PostMapping("/login")
    public ApiResponse login(@RequestBody LoginRequestDto request) {
        String userId = memberService.login(request.getEmail(), request.getPassword());
        return ApiResponse.success(200, "Login Successful", userId);
    }

    @PostMapping("/logout")
    public ApiResponse logout(@RequestBody String userId) {
        return ApiResponse.success(200,"Logout Successful", memberService.logout(userId));
    }
}
