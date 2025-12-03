package com.blublu.cart.repository;

import com.blublu.cart.document.CartDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CartRepository extends MongoRepository<CartDocument, Long>, CustomCartRepository {
  CartDocument findByUsername(String username);
  Long deleteTopByUsername(String username);
}
