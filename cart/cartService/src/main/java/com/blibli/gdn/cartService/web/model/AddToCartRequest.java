package com.blibli.gdn.cartService.web.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddToCartRequest {
    @NotBlank(message = "SKU is required")
    private String sku;
    
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer qty;
}
