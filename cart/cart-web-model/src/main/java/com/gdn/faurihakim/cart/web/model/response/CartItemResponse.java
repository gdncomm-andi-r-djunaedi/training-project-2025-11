package com.gdn.faurihakim.cart.web.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartItemResponse {
    private String productId;
    private String productName;
    private Double price;
    private Integer quantity;
    private Double subtotal;
}
