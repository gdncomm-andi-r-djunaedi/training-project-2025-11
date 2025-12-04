package com.marketplace.member.command;

import com.marketplace.common.command.Command;
import com.marketplace.common.dto.UserDetailsResponse;
import com.marketplace.common.dto.ValidateCredentialsRequest;
import com.marketplace.member.service.MemberService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ValidateCredentialsCommand implements Command<UserDetailsResponse> {

    private final MemberService memberService;
    private final ValidateCredentialsRequest request;

    @Override
    public UserDetailsResponse execute() {
        return memberService.validateCredentials(request);
    }
}
