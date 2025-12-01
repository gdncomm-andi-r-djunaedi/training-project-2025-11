package com.blublu.product.document;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("product_detail")
public class ProductDetail {

  @Id
  private String id;
  private String productId;
  private String name;
  private String description;
  private BigDecimal price;
  private BigDecimal originalPrice;
  private List<String> categories;
}
