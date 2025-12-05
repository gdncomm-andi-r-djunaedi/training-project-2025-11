package com.sc.cartservice.repository;

import com.sc.cartservice.model.Cart;
import com.sc.cartservice.model.CartItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemRepository extends MongoRepository<CartItem, String> {
}
