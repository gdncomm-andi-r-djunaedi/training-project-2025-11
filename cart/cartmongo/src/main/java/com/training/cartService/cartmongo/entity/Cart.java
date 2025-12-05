package com.training.cartService.cartmongo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = Cart.COLLECTION_NAME)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Cart {
    public static final String COLLECTION_NAME = "cart";

    @Id
    private String userId;
    private double totalPrice;
    private int totalQuantity;
    private List<CartItemEntity> items;
}
