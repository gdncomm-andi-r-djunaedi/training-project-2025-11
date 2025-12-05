package com.project.cart.dto.response;

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
@Schema(description = "Cart information response")
public class CartResponse {

    @Schema(description = "Cart ID", example = "user123")
    private String cartId;

    @Schema(description = "User ID", example = "user123")
    private String userId;

    @Schema(description = "List of items in cart")
    private List<CartItemResponse> items;

    @Schema(description = "Total number of items", example = "5")
    private Integer totalItems;

    @Schema(description = "Total cart amount", example = "1499.95")
    private BigDecimal totalAmount;

    @Schema(description = "Creation timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
