package com.blibli.cart.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Cart entity - Persisted in MongoDB, Cached in Redis
 * MongoDB: Source of truth for cart data  
 * Redis: Fast cache with TTL for frequent reads
 */
@Document(collection = "carts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cart implements Serializable {

    @Id
    private String userId; // UUID from Member service (also MongoDB ID)

    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    private Date createdAt;
    private Date updatedAt;
}
