package com.gdn.project.waroenk.cart.dto.systemparameter;

import jakarta.validation.constraints.NotBlank;

public record UpsertSystemParameterRequestDto(
    @NotBlank(message = "Variable name is required") String variable,
    @NotBlank(message = "Data is required") String data,
    String description,
    String type
) {}




