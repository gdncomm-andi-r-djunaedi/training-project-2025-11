package com.blibli.cart.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Items {
    private String productName;
    private String productSku;
    private String productDesc;
    private String productBrand;
    private String productCategory;
    private Double productPrice;
    private Integer quantity;
}
