package com.ecom.product.Dto;


import com.ecom.product.Entitiy.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProductDto {

    private String productSku;
    private String name;
    private Double price;
    private String image;
    private String description;

}
