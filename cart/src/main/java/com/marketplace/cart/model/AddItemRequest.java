package com.marketplace.cart.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AddItemRequest {
  private String customerId;
  private CartItem item;
}
