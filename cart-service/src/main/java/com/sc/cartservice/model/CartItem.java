package com.sc.cartservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "cartProducts")
public class CartItem {
    @Id
    private String cartItemId = UUID.randomUUID().toString();

    private String productCode;

    private String productName;

    private double productPrice;

    private int quantity;
}
