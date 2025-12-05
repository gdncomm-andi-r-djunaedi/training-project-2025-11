package com.gdn.project.waroenk.member.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthenticateRequestDto(
    @NotBlank(message = "User (email or phone) is required") String user,
    @NotBlank(message = "Password is required") String password) {
}







