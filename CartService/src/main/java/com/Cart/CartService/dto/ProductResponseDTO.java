package com.Cart.CartService.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponseDTO {
    @NotBlank(message = "Product ID is mandatory")
    private String productId;

    @NotBlank(message = "Product code is mandatory")
    private String productCode;

    @NotBlank(message = "Product name is mandatory")
    @Size(min = 3, max = 50, message = "Product name must be between 3 to 50 characters")
    private String productName;

    @Positive(message = "Price must be greater than 0")
    private double price;

    @NotBlank(message = "Category is mandatory")
    private String category;

    @NotBlank(message = "Product description is mandatory")
    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String productDescription;
}
