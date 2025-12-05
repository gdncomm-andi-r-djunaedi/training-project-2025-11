package com.dev.onlineMarketplace.cart.repository;

import com.dev.onlineMarketplace.cart.model.Cart;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends MongoRepository<Cart, String> {
}
