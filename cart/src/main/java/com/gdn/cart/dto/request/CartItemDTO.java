package com.gdn.cart.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CartItemDTO {
    private String productId;
    private String productName;
    private Double price;
    private Integer quantity;
}
