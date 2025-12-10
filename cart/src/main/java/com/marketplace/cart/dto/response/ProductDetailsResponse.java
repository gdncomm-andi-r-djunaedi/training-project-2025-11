package com.marketplace.cart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for product details fetched from product service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailsResponse {
    private String id;
    private String name;
    private BigDecimal price;
}
