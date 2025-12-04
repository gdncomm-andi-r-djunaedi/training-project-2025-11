package com.gdn.training.apigateway.application.usecase.model;

public record TokenData(
        String token,
        String jti,
        long expiration) {

}
