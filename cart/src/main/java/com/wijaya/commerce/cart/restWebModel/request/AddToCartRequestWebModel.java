package com.wijaya.commerce.cart.restWebModel.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddToCartRequestWebModel {
    @NotBlank(message = "User ID is required")
    private String userId;
    private String cartId;
    @NotBlank(message = "Product SKU is required")
    private String productSku;

    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}
