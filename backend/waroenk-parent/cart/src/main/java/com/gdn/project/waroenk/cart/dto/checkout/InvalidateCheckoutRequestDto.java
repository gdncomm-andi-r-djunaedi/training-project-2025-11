package com.gdn.project.waroenk.cart.dto.checkout;

import jakarta.validation.constraints.NotBlank;

public record InvalidateCheckoutRequestDto(
    @NotBlank(message = "Checkout ID is required") String checkoutId
) {}




