package com.example.cart.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = Cart.COLLECTION_NAME)
public class Cart {
    public static final String COLLECTION_NAME="CART";

    @Id
    private ObjectId id;
    private Double totalPrice;
    List<Product> cartItems;
}
