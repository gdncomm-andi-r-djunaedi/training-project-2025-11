package com.gdn.cart.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = CartItem.COLLECTION_NAME)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CartItem {
     static final String COLLECTION_NAME = "cartItem";
    private String productId;
    private String productName;
    private Double price;
    private Integer quantity;

}
