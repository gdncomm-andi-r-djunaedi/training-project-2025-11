package com.example.cart.repository;


import com.example.cart.entity.CartEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<CartEntity,Long> {


    CartEntity findByCustomerIdAndProductId(UUID customerId,String productId);

    Page<CartEntity> findAllByCustomerId(Pageable pageable, UUID CustomerId);

    void deleteAllByCustomerId(UUID customerId);

    void deleteAllByProductId(String productId);

    void deleteAllByCustomerIdAndProductId(UUID customerId,String productId);

    List<CartEntity> findAllByProductId(String productId);
}
