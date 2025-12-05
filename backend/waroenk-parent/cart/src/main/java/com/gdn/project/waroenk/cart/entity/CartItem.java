package com.gdn.project.waroenk.cart.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Embedded document representing an item within a cart.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {
    private String sku;
    private Integer quantity;
    private Long priceSnapshot;
    private String title;
    private String imageUrl;
    private Map<String, String> attributes;
}




