package com.blibli.cart.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponseDTO {
    private String userId; // UUID from Member service
    private List<CartItemResponseDTO> items;
    private BigDecimal totalAmount;
    private Integer totalItems;
    private LocalDateTime updatedAt;
}
