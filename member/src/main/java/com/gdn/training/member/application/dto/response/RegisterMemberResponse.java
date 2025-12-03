package com.gdn.training.member.application.dto.response;

import java.util.UUID;

public record RegisterMemberResponse(
        UUID memberId,
        String fullName,
        String email) {
}
