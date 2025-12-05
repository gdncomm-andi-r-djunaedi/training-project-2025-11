package com.example.cart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import com.example.cart.model.Cart;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUserId(Long userId);
}
