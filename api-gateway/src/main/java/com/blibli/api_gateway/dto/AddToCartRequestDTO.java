package com.blibli.api_gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddToCartRequestDTO {
    private String productSku;
    private String productName;
    private Integer quantity;
}
