package com.gdn.project.waroenk.member.dto;

import com.gdn.project.waroenk.member.validation.StrongPasswordRequired;
import jakarta.validation.constraints.NotBlank;

@StrongPasswordRequired(passwordField = "newPassword")
public record ChangePasswordRequestDto(
    @NotBlank(message = "Reset token is required") String resetToken,
    @NotBlank(message = "New password is required") String newPassword,
    @NotBlank(message = "Confirm password is required") String confirmPassword) {
}



