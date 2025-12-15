package com.project.product.dto.response;

import com.project.product.dto.ProductImageDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Product information response")
public class ProductResponse {
    @Schema(description = "Product ID", example = "507f1f77bcf86cd799439011")
    private String id;

    @Schema(description = "Stock Keeping Unit", example = "PROD-001")
    private String sku;

    @Schema(description = "Product name", example = "Premium Wireless Headphones")
    private String name;

    @Schema(description = "URL-friendly slug", example = "premium-wireless-headphones")
    private String slug;

    @Schema(description = "Product description")
    private String description;

    @Schema(description = "Product price", example = "299.99")
    private BigDecimal price;

    @Schema(description = "Discounted price", example = "249.99")
    private BigDecimal discountPrice;

    @Schema(description = "Product category", example = "Electronics")
    private String category;

    @Schema(description = "Product tags")
    private List<String> tags;

    @Schema(description = "Product images")
    private ProductImageDto images;

    @Schema(description = "View count", example = "1523")
    private Integer viewCount;

    @Schema(description = "Average rating", example = "4.5")
    private BigDecimal rating;

    @Schema(description = "Number of reviews", example = "87")
    private Integer reviewCount;

    @Schema(description = "Active status", example = "true")
    private Boolean isActive;

    @Schema(description = "Creation timestamp", example = "2024-01-15T10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2024-01-20T14:45:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
