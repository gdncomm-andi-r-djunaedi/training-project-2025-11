package com.example.member.controllers;

import com.example.member.dto.UserRequestDto;
import com.example.member.dto.UserResponseDTO;
import com.example.member.service.UserService;
import com.example.member.utils.APIResponse;
import com.example.member.utils.ResponseUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/member")
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping("/register")
    public ResponseEntity<APIResponse<UserResponseDTO>> registerUser(@Valid @RequestBody UserRequestDto userRequestDto){
        return ResponseEntity.ok(ResponseUtil.success(HttpStatus.OK.value(), HttpStatus.OK, userService.registerUser(userRequestDto)));
    }

    @PostMapping("/login")
    public ResponseEntity<APIResponse<String>> loginUser(@RequestBody com.example.member.dto.LoginRequestDto loginRequestDto, jakarta.servlet.http.HttpServletResponse response){
        String token = userService.loginUser(loginRequestDto);

        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("Authorization", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60); // 1 day
        response.addCookie(cookie);

        return ResponseEntity.ok(ResponseUtil.success(HttpStatus.OK.value(), HttpStatus.OK, "Login successful"));
    }
}
