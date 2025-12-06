package com.gdn.training.apigateway.application.usecase.model;

public record LoginResult(
        Boolean success,
        String memberId,
        String fullName,
        String email) {
}
