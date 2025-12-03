package com.gdn.training.member.application.usecase;

import com.gdn.training.member.application.dto.request.LoginMemberRequest;
import com.gdn.training.member.application.dto.response.LoginMemberResponse;

public interface LoginMemberUseCase {
    LoginMemberResponse login(LoginMemberRequest request);
}
