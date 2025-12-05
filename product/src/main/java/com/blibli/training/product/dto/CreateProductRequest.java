package com.blibli.training.product.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CreateProductRequest {
  @NotBlank
  private String productName;

  @NotBlank
  private String categoryName;

  private int stock;
}
