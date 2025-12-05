package com.example.marketplace.cart.repository;

import com.example.marketplace.cart.entity.CartItem;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CartRepository extends MongoRepository<CartItem, String> {

    List<CartItem> findByUserId(String userId);

    void deleteByUserIdAndProductId(String userId, String productId);
}
