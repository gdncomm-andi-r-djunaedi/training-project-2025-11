package com.blibli.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import javax.swing.text.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateProductRequestDTO {
    private String productName;
    private String productSku;
    private String productDesc;
    private String productBrand;
    private String productCategory;
    private Double productPrice;
}
