package com.gdn.training.member.application.dto.request;

public record RegisterMemberRequest(
        String fullName,
        String email,
        String password,
        String phoneNumber) {
}
