package com.gdn.cart.controller.webmodel.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {

  private String cartId;

  private String memberId;

  private List<CartItemResponse> items;

  private BigDecimal totalPrice;

  private Integer totalItems;
}

