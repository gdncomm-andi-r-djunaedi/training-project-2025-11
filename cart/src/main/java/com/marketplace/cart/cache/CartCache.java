package com.marketplace.cart.cache;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CartCache implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID memberId;
    
    @Builder.Default
    private List<CartItemCache> items = new ArrayList<>();
    
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;
    
    @Builder.Default
    private Integer totalItems = 0;
    
    private LocalDateTime lastModified;
    
    @Builder.Default
    private boolean dirty = false;

    public void recalculateTotals() {
        this.totalAmount = items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalItems = items.stream()
                .mapToInt(CartItemCache::getQuantity)
                .sum();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CartItemCache implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private String productId;
        private String productName;
        private BigDecimal price;
        private Integer quantity;
        private String productImage;

        public BigDecimal getSubtotal() {
            return price.multiply(BigDecimal.valueOf(quantity));
        }
    }
}

