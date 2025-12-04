package com.blibli.cart.entity;

import lombok.*;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem implements Serializable {

    private String productId;
    private String sku;
    private String name;
    private BigDecimal price;
    private Integer quantity;
    private Date addedAt;


}
