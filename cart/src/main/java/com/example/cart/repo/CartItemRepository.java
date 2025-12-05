package com.example.cart.repo;

import com.example.cart.entity.CartItem;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CartItemRepository extends MongoRepository<CartItem, String> {
    List<CartItem> findByUsername(String username);
    void deleteByUsernameAndSku(String username, String sku);
}
