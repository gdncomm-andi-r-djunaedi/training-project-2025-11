package com.example.cart.entity;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = Cart.COLLECTION_NAME)

public class Cart {

    public static final String COLLECTION_NAME = "cart";

    public Cart(String userId) {
        this.userId = userId;
        this.items = new ArrayList<>();
    }

    @Id
    private String userId;
    private List<CartItem> items;

}
