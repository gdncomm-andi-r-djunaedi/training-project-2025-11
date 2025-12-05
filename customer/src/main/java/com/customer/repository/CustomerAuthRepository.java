package com.customer.repository;

import com.customer.entity.CustomerAuth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerAuthRepository extends JpaRepository<CustomerAuth, UUID> {
    Optional<CustomerAuth> findByEmailIgnoreCase(String email);
    void deleteByCustomerId(UUID customerId);
}
