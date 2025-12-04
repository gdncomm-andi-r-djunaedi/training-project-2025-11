package com.marketplace.member.command;

import com.marketplace.common.command.Command;
import com.marketplace.member.dto.MemberResponse;
import com.marketplace.member.dto.RegisterRequest;
import com.marketplace.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegisterMemberCommand implements Command<RegisterRequest, MemberResponse> {

    private final MemberService memberService;

    @Override
    public MemberResponse execute(RegisterRequest request) {
        return memberService.register(request);
    }
}
