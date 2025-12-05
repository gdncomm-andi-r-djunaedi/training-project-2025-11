package com.gdn.project.waroenk.member.dto;

import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequestDto(
    @NotBlank(message = "Phone or email is required") String phoneOrEmail) {
}



