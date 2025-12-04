package com.zasura.cart.repository;

import com.zasura.cart.entity.CartProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartProductRepository extends JpaRepository<CartProduct, Long> {
  CartProduct findByProductIdAndCartId(String productId, Long cartId);
}
