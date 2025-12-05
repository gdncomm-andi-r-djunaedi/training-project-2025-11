package com.gdn.project.waroenk.cart.dto.cart;

import java.util.Map;

public record CartItemDto(
    String sku,
    Integer quantity,
    Long priceSnapshot,
    String title,
    String imageUrl,
    Map<String, String> attributes
) {}




