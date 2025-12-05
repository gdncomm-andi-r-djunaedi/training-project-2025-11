package com.gdn.project.waroenk.member.dto;

public record UserTokenResponseDto(
    String accessToken,
    String tokenType,
    Long expiresIn,
    String userId) {
}







