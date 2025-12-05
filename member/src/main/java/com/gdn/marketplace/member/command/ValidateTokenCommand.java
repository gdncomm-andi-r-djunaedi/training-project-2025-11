package com.gdn.marketplace.member.command;

import com.gdn.marketplace.member.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValidateTokenCommand implements Command<String, String> {

    @Autowired
    private MemberService memberService;

    @Override
    public String execute(String token) {
        memberService.validateToken(token);
        return "Token is valid";
    }
}
