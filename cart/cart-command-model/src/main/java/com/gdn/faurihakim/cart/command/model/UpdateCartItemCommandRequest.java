package com.gdn.faurihakim.cart.command.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCartItemCommandRequest {
    private String cartId;
    private String productId;
    private Integer quantity;
}
