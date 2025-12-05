package com.gdn.project.waroenk.member.dto;

public record ForgotPasswordResponseDto(
    boolean success,
    String message,
    String resetToken,
    Long expiresInSeconds) {
}



