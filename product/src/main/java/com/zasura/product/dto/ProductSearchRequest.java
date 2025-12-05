package com.zasura.product.dto;

import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchRequest {
  private String name;
  private String description;
  @DecimalMin("0.0")
  private Double minPrice;
  @DecimalMin("0.0")
  private Double maxPrice;
  private Pagination pagination;
}
