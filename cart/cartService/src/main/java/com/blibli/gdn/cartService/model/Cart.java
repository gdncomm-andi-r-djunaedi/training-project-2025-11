package com.blibli.gdn.cartService.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "carts")
public class Cart {
    @Id
    private String id;

    @Indexed(unique = true)
    private String memberId;

    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    private Integer totalItems;
    private BigDecimal totalValue;
    private String currency;
    private Instant updatedAt;
    private Instant expireAt;
}
