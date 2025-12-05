package com.example.cart.dto;

//import com.example.product.dto.ProductDTO;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemResponse {
    private String productId;
    private Integer quantity;
    private BigDecimal subTotalPrice;
}
