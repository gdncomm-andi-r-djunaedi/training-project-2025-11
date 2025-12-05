package com.gdn.project.waroenk.cart.dto.checkout;

import jakarta.validation.constraints.NotBlank;

public record FinalizeCheckoutRequestDto(
    @NotBlank(message = "Checkout ID is required") String checkoutId,
    @NotBlank(message = "Order ID is required") String orderId
) {}




