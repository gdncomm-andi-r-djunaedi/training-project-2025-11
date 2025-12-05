package com.marketplace.cart.repository;

import com.marketplace.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByMemberId(UUID memberId);

    boolean existsByMemberId(UUID memberId);

    void deleteByMemberId(UUID memberId);

    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.memberId = :memberId")
    Optional<Cart> findByMemberIdWithItems(UUID memberId);
}

