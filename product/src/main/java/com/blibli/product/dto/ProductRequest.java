package com.blibli.product.dto;

import com.blibli.product.enums.CategoryType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    @NotBlank(message = "SKU is required")
    private String sku;

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @NotNull(message = "Category is required")
    private CategoryType category;

    @PositiveOrZero(message = "Stock quantity must be zero or positive")
    @Max(value = 2147483647, message = "Stock quantity cannot exceed 2,147,483,647")
    private Integer stockQuantity;
}
