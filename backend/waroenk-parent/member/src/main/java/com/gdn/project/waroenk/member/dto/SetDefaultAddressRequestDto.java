package com.gdn.project.waroenk.member.dto;

import jakarta.validation.constraints.NotBlank;

public record SetDefaultAddressRequestDto(
    @NotBlank(message = "User ID is required") String userId,
    @NotBlank(message = "Address label is required") String label) {
}







