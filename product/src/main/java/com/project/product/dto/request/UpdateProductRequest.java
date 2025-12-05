package com.project.product.dto.request;

import com.project.product.dto.ProductImageDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for updating a product")
public class UpdateProductRequest {
    @Size(min = 3, max = 200, message = "Product name must be between 3 and 200 characters")
    @Schema(description = "Product name", example = "Premium Wireless Headphones v2")
    private String name;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    @Schema(description = "Detailed product description")
    private String description;

    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Price must have at most 10 integer digits and 2 decimal places")
    @Schema(description = "Product price", example = "349.99")
    private BigDecimal price;

    @DecimalMin(value = "0.01", message = "Discount price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Discount price must have at most 10 integer digits and 2 decimal places")
    @Schema(description = "Discounted price", example = "299.99")
    private BigDecimal discountPrice;

    @Size(min = 2, max = 100, message = "Category must be between 2 and 100 characters")
    @Schema(description = "Product category", example = "Electronics")
    private String category;

    @Schema(description = "Product tags for filtering")
    private List<String> tags;

    @Valid
    @Schema(description = "Product images")
    private ProductImageDto images;

    @Schema(description = "Product active status", example = "true")
    private Boolean isActive;
}
