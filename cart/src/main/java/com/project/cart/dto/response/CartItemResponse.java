package com.project.cart.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Cart item information")
public class CartItemResponse {

    @Schema(description = "Product ID", example = "507f1f77bcf86cd799439011")
    private String productId;

    @Schema(description = "Product SKU", example = "PROD-001")
    private String productSku;

    @Schema(description = "Product name", example = "Premium Wireless Headphones")
    private String productName;

    @Schema(description = "Product image URL")
    private String productImage;

    @Schema(description = "Quantity", example = "2")
    private Integer quantity;

    @Schema(description = "Unit price", example = "299.99")
    private BigDecimal price;

    @Schema(description = "Subtotal", example = "599.98")
    private BigDecimal subtotal;

    @Schema(description = "Added timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime addedAt;
}
