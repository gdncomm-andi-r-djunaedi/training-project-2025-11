package com.marketplace.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body untuk menambahkan item ke cart")
public class AddToCartRequest {

    @Schema(description = "Product ID (MongoDB ObjectId)", example = "507f1f77bcf86cd799439011", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Product ID is required")
    private String productId;

    @Schema(description = "Nama produk untuk display", example = "iPhone 15 Pro", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Product name is required")
    private String productName;

    @Schema(description = "Harga produk per unit", example = "19999000.00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price must be positive")
    private BigDecimal price;

    @Schema(description = "Jumlah item yang akan ditambahkan", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @Schema(description = "URL gambar produk", example = "https://example.com/product-image.jpg")
    private String productImage;
}
