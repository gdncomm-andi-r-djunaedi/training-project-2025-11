package com.gdn.marketplace.member.command;

import com.gdn.marketplace.member.dto.AuthRequest;
import com.gdn.marketplace.member.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LoginMemberCommand implements Command<String, AuthRequest> {

    @Autowired
    private MemberService memberService;

    @Override
    public String execute(AuthRequest request) {
        if (memberService.validateUser(request)) {
            return memberService.generateToken(request.getUsername());
        } else {
            throw new RuntimeException("invalid access");
        }
    }
}
