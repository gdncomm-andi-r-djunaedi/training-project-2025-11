package com.gdn.project.waroenk.cart.dto.checkout;

public record CheckoutItemDto(
    String sku,
    Integer quantity,
    Long priceSnapshot,
    String title,
    Boolean reserved
) {}




