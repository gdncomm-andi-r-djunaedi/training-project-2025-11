package com.gdn.project.waroenk.cart.dto.checkout;

import jakarta.validation.constraints.NotBlank;

public record ValidateCheckoutRequestDto(
    @NotBlank(message = "User ID is required") String userId
) {}




