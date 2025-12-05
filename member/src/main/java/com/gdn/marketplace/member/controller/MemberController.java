package com.gdn.marketplace.member.controller;

import com.gdn.marketplace.member.dto.AuthRequest;
import com.gdn.marketplace.member.command.LoginMemberCommand;
import com.gdn.marketplace.member.command.RegisterMemberCommand;
import com.gdn.marketplace.member.command.ValidateTokenCommand;
import com.gdn.marketplace.member.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    @Autowired
    private RegisterMemberCommand registerMemberCommand;

    @Autowired
    private LoginMemberCommand loginMemberCommand;

    @Autowired
    private ValidateTokenCommand validateTokenCommand;

    @PostMapping("/register")
    public String addNewUser(@RequestBody AuthRequest authRequest) {
        return registerMemberCommand.execute(authRequest);
    }

    @PostMapping("/login")
    public String getToken(@RequestBody AuthRequest authRequest) {
        return loginMemberCommand.execute(authRequest);
    }

    @GetMapping("/validate")
    public String validateToken(@RequestParam("token") String token) {
        return validateTokenCommand.execute(token);
    }
}
