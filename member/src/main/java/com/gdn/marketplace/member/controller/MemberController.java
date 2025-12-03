package com.gdn.marketplace.member.controller;

import com.gdn.marketplace.member.dto.AuthRequest;
import com.gdn.marketplace.member.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    @Autowired
    private MemberService service;

    @PostMapping("/register")
    public String addNewUser(@RequestBody AuthRequest authRequest) {
        return service.saveMember(authRequest);
    }

    @PostMapping("/login")
    public String getToken(@RequestBody AuthRequest authRequest) {
        if (service.validateUser(authRequest)) {
            return service.generateToken(authRequest.getUsername());
        } else {
            throw new RuntimeException("invalid access");
        }
    }

    @GetMapping("/validate")
    public String validateToken(@RequestParam("token") String token) {
        service.validateToken(token);
        return "Token is valid";
    }
}
