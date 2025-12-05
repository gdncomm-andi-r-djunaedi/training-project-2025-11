package com.zasura.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddProductResponse {
  private Long cartId;
  private String productId;
  private Integer quantity;
}
