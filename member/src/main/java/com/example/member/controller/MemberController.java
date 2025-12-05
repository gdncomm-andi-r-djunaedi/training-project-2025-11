package com.example.member.controller;

import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import com.example.member.service.UserService;
import com.example.member.model.User;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        return ResponseEntity.ok(userService.register(user));
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody LoginReq req) {
        return ResponseEntity.ok(userService.login(req.getEmail(), req.getPassword()));
    }
}

class LoginReq {
    private String email;
    private String password;
    public String getEmail() { return email; }
    public String getPassword() { return password; }
}
