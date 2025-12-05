package com.marketplace.cart.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RemoveItemRequest {
  private String customerId;
  private String productId;
}
