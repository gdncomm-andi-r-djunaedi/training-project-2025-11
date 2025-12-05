package com.gdn.project.waroenk.member.dto;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequestDto(
    @NotBlank(message = "User ID is required") String userId,
    String accessToken) {
}



