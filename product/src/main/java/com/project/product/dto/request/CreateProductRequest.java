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
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for creating a new product")
public class CreateProductRequest {
    @NotBlank(message = "SKU is required")
    @Size(min = 3, max = 50, message = "SKU must be between 3 and 50 characters")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "SKU must contain only uppercase letters, numbers, and hyphens")
    @Schema(description = "Stock Keeping Unit - unique identifier", example = "PROD-001")
    private String sku;

    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 200, message = "Product name must be between 3 and 200 characters")
    @Schema(description = "Product name", example = "Premium Wireless Headphones")
    private String name;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    @Schema(description = "Detailed product description",
            example = "High-quality wireless headphones with noise cancellation")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Price must have at most 10 integer digits and 2 decimal places")
    @Schema(description = "Product price", example = "299.99")
    private BigDecimal price;

    @DecimalMin(value = "0.01", message = "Discount price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Discount price must have at most 10 integer digits and 2 decimal places")
    @Schema(description = "Discounted price (optional)", example = "249.99")
    private BigDecimal discountPrice;

    @NotBlank(message = "Category is required")
    @Size(min = 2, max = 100, message = "Category must be between 2 and 100 characters")
    @Schema(description = "Product category", example = "Electronics")
    private String category;

    @Schema(description = "Product tags for filtering", example = "[\"wireless\", \"audio\", \"premium\"]")
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @Valid
    @Schema(description = "Product images")
    private ProductImageDto images;

    @Schema(description = "Product active status", example = "true")
    @Builder.Default
    private Boolean isActive = true;
}
