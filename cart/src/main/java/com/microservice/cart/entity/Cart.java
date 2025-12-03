package com.microservice.cart.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@Document(collection = Cart.COLLECTION_NAME)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Cart {

    public static final String COLLECTION_NAME = "carts";

    @Id
    private String id;

    private Long userId;

    private List<CartItem> items;

    private Date updatedAt;
}
