package com.example.member.controllers;

import com.example.member.dto.UserRequestDto;
import com.example.member.dto.UserResponseDTO;
import com.example.member.service.UserService;
import com.example.member.utils.APIResponse;
import com.example.member.utils.ResponseUtil;
import jakarta.servlet.http.Cookie;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/member")
@Slf4j
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping("/register")
    public ResponseEntity<APIResponse<UserResponseDTO>> registerUser(@Valid @RequestBody UserRequestDto userRequestDto){
        return ResponseEntity.ok(ResponseUtil.success(userService.registerUser(userRequestDto)));
    }

    @PostMapping("/login")
    public ResponseEntity<APIResponse<String>> loginUser(@RequestBody com.example.member.dto.LoginRequestDto loginRequestDto, jakarta.servlet.http.HttpServletResponse response){
        String token = userService.loginUser(loginRequestDto);
        log.info("token:" + token);
        Cookie cookie = new Cookie("Authorization", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60); // 1 day
        response.addCookie(cookie);

        return ResponseEntity.ok(ResponseUtil.success("Login successful"));
    }

    @GetMapping("/profile")
    public ResponseEntity<APIResponse<UserResponseDTO>> getMemberProfile(@RequestHeader("x-user-id") String userId){
        return ResponseEntity.ok(ResponseUtil.success(userService.getMemberProfile(userId)));
    }
}
