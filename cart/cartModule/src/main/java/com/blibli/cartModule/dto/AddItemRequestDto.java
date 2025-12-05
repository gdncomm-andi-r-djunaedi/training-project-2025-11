package com.blibli.cartModule.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddItemRequestDto {

  private String productId;
  private Integer quantity;
}

