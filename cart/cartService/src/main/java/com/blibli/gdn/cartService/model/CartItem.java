package com.blibli.gdn.cartService.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    private String sku;
    private String productId;
    private String name;
    private BigDecimal price;
    private String currency;
    private Integer qty;
    private Instant addedAt;
    private Map<String, Object> variant;
}
