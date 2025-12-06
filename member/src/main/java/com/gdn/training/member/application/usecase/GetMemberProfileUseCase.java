package com.gdn.training.member.application.usecase;

import java.util.UUID;

import com.gdn.training.member.application.dto.response.MemberProfileResponse;

public interface GetMemberProfileUseCase {
    MemberProfileResponse getProfile(UUID memberId);
}
