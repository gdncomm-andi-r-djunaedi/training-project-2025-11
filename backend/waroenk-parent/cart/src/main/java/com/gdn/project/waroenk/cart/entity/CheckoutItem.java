package com.gdn.project.waroenk.cart.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Embedded document representing an item within a checkout.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutItem {
    private String sku;
    private Integer quantity;
    private Long priceSnapshot;
    private String title;
    
    @Builder.Default
    private Boolean reserved = false;
}




