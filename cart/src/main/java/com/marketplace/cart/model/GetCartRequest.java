package com.marketplace.cart.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GetCartRequest {
  private String customerId;
}
