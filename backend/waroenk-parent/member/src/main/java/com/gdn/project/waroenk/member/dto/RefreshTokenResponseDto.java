package com.gdn.project.waroenk.member.dto;

public record RefreshTokenResponseDto(
    String accessToken,
    String refreshToken,
    String tokenType,
    Long expiresIn,
    String userId) {
}



