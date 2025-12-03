package com.gdn.training.member.application.dto.response;

import java.util.UUID;

public record LoginMemberResponse(
                boolean success,
                UUID memberId,
                String fullName,
                String email) {
}
