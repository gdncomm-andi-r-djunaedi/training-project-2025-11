package com.zasura.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.NumberFormat;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchRequest {
  private String name;
  private String description;
  @DecimalMin("0.0")
  @NumberFormat
  private Double minPrice;
  @DecimalMin("0.0")
  @NumberFormat
  private Double maxPrice;

}
