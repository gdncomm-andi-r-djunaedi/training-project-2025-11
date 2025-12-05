package com.blibli.training.member.controller;

import com.blibli.training.framework.dto.BaseResponse;
import com.blibli.training.member.dto.LoginRequest;
import com.blibli.training.member.dto.LoginResponse;
import com.blibli.training.member.dto.RegisterRequest;
import com.blibli.training.member.entity.Member;
import com.blibli.training.member.service.MemberService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/register")
    public BaseResponse<Member> register(@RequestBody RegisterRequest request) {
        return BaseResponse.success(memberService.register(request));
    }

    @PostMapping("/login")
    public BaseResponse<LoginResponse> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        LoginResponse loginResponse = memberService.login(request);

        // Set Cookie
        Cookie cookie = new Cookie("token", loginResponse.getToken());
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Set to true in production
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60); // 1 day
        response.addCookie(cookie);

        return BaseResponse.success(loginResponse);
    }

    @PostMapping("/logout")
    public BaseResponse<Void> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("token", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return BaseResponse.success(null);
    }

    @GetMapping("/hello")
    public String helloWorld() {
        return new String("Hello World");
    }
    @GetMapping("/hello-protected")
    public String helloWorldPrivate() {
        return new String("Hello World Private");
    }
    
}
