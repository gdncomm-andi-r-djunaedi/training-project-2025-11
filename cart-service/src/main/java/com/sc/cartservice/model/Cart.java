package com.sc.cartservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "cart")
public class Cart {
    @Id
    private String cartId = UUID.randomUUID().toString();

    private String memberId;

    private List<CartItem> cartItems;

    private double totalPrice;

    public double calculateTotalPrice() {
        double total = 0.0;
        for (CartItem p : cartItems) {
            total += p.getProductPrice() * p.getQuantity();
        }
        return total;
    }
}
