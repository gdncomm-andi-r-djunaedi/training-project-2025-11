package com.gdn.faurihakim.cart.client.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetProductResponse {
    private String productId;
    private String productName;
    private Double price;
}
