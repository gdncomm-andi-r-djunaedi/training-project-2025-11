package com.gdn.training.apigateway.application.usecase.model;

import java.time.Instant;

public record MemberProfile(
        String memberId,
        String email,
        String fullName,
        String phoneNumber,
        String address,
        Instant createdAt,
        Instant updatedAt) {

}
