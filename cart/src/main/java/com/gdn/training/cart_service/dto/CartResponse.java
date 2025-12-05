package com.gdn.training.cart_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartResponse {
    private String id;

    private Long memberId;

    private List<CartItemResponse> items;

    private BigDecimal totalPrice;

    private Integer totalItems;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
