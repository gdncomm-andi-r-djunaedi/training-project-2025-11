package com.gdn.training.member.application.dto.request;

public record LoginMemberRequest(
        String email,
        String rawPassword) {
}
