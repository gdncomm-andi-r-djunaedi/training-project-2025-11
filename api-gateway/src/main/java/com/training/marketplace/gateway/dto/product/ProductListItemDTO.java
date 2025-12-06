package com.training.marketplace.gateway.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductListItemDTO {
    private String productId;
    private String productName;
    private double productPrice;
    private String productImage;
}
