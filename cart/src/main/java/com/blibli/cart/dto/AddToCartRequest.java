package com.blibli.cart.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddToCartRequest {

//    @NotBlank(message = "Product ID is required")
//    private String productId;
//
//    @NotNull(message = "Quantity is required")
//    @Positive(message = "Quantity must be positive")
//    @Builder.Default
//    private Integer quantity = 1;

    @NotBlank(message = "Product ID is required")
    private String productId;
    private String productName;
    private String sku;
    @Builder.Default
    private int quantity= 1;
}
