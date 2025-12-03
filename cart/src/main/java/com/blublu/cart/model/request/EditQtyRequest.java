package com.blublu.cart.model.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EditQtyRequest {
  private String skuCode;
  private int newQty;
}
