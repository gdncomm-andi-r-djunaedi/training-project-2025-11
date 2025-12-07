package com.Cart.CartService.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(Cart.TABLE_NAME)
public class Cart {
    public static final String TABLE_NAME = "Cart";
    @Id
    private String id;
    private String cartId;
    private String memberId;
    private List<CartItem> items = new ArrayList<>();
}
