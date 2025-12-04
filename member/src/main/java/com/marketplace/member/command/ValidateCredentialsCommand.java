package com.marketplace.member.command;

import com.marketplace.common.command.Command;
import com.marketplace.common.dto.UserDetailsResponse;
import com.marketplace.common.dto.ValidateCredentialsRequest;
import com.marketplace.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ValidateCredentialsCommand implements Command<ValidateCredentialsRequest, UserDetailsResponse> {

    private final MemberService memberService;

    @Override
    public UserDetailsResponse execute(ValidateCredentialsRequest request) {
        return memberService.validateCredentials(request);
    }
}
