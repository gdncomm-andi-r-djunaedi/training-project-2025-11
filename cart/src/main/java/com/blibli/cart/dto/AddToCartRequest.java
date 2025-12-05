package com.blibli.cart.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddToCartRequest {

    @NotBlank(message = "Product ID is required")
    private String productId;
    
    private String productName;
    
    @Pattern(regexp = "^[A-Za-z]{3}-\\d{5}-\\d{5}$", 
             message = "SKU must match format: AAA-#####-##### (e.g., ABC-12345-67890)")
    private String sku;
    
    @Positive(message = "Quantity must be positive")
    @Builder.Default
    private int quantity = 1;
}
