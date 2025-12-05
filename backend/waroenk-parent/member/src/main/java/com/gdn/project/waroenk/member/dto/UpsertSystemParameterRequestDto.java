package com.gdn.project.waroenk.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpsertSystemParameterRequestDto(
    @NotBlank(message = "Variable is required") @Size(max = 255) String variable,
    @NotNull(message = "Data is required") String data, String description) {
}
