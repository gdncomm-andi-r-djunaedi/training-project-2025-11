package com.gdn.training.member.application.dto.response;

import java.util.UUID;

public record MemberProfileResponse(
                UUID memberId,
                String fullName,
                String email,
                String phoneNumber,
                String avatarUrl) {
}
