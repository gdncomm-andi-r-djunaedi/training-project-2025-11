package com.gdn.project.waroenk.cart.dto.checkout;

public record FinalizeCheckoutResponseDto(
    Boolean success,
    String message,
    String orderId
) {}




