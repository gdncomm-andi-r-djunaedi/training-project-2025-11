package com.ecom.cart.Entity;

import com.ecom.cart.Dto.CartItemDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = Cart.COLLECTION_NAME)
//@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Cart {
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public static final String COLLECTION_NAME = "cart";

    @Id
    private String id;
    private String userId;
    private List<CartItem> items;

}
