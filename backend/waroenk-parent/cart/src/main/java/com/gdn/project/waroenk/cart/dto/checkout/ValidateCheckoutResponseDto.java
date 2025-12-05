package com.gdn.project.waroenk.cart.dto.checkout;

import java.util.List;

public record ValidateCheckoutResponseDto(
    CheckoutResponseDto checkout,
    Boolean success,
    String message,
    List<ValidationErrorDto> errors
) {
    public record ValidationErrorDto(
        String sku,
        String errorCode,
        String message
    ) {}
}




