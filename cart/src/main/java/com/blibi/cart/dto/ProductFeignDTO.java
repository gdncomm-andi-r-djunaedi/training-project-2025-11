package com.blibi.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ProductFeignDTO {

    private String productId;
    private String productName;
    private double productUnitPrice;
}
