package com.kailash.cart.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class AddItemRequest {
    private String sku;
    private int qty;

}
