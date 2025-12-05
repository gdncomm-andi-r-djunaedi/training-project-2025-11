package com.project.cart.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Cart entity stored in MongoDB for persistence
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "carts")
public class Cart {

    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;

    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    @Builder.Default
    private Integer totalItems = 0;

    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Indexed
    private CartStatus status;

    private String sessionId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Indexed
    private LocalDateTime expiresAt;

    private String convertedToOrderId;

    private CartMetadata metadata;
}
