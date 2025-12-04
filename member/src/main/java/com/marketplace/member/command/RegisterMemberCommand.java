package com.marketplace.member.command;

import com.marketplace.common.command.Command;
import com.marketplace.member.dto.MemberResponse;
import com.marketplace.member.dto.RegisterRequest;
import com.marketplace.member.service.MemberService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RegisterMemberCommand implements Command<MemberResponse> {

    private final MemberService memberService;
    private final RegisterRequest request;

    @Override
    public MemberResponse execute() {
        return memberService.register(request);
    }
}
