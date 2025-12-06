package com.gdn.training.member.application.mapper;

import com.gdn.training.member.application.dto.response.MemberProfileResponse;
import com.gdn.training.member.domain.model.Member;

public final class MemberMapper {
    private MemberMapper() {
    }

    public static MemberProfileResponse toMemberProfileResponse(Member member) {
        if (member == null) {
            return null;
        }
        return new MemberProfileResponse(
                member.getId(),
                member.getFullName(),
                member.getEmail(),
                member.getPhoneNumber(),
                member.getAvatarUrl());
    }
}
