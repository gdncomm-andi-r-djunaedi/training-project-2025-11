package com.gdn.training.member.application.usecase;

import com.gdn.training.member.application.dto.request.RegisterMemberRequest;
import com.gdn.training.member.application.dto.response.RegisterMemberResponse;

public interface RegisterMemberUseCase {
    RegisterMemberResponse register(RegisterMemberRequest request);
}
