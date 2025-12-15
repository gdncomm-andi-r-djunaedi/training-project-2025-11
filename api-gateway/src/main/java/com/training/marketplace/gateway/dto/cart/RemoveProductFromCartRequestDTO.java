package com.training.marketplace.gateway.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RemoveProductFromCartRequestDTO {
    private String userId;
    private String productId;
    private int quantity;
}
