package com.elfrida.cart.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "carts")
public class Cart {

    @Id
    private String id;

    private String memberId;

    private List<CartItem> items = new ArrayList<>();

    private Instant createdAt;
    private Instant updatedAt;
}


