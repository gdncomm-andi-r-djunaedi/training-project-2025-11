package com.example.product.entity;

import com.example.product.model.ProductTag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "products")
public class Product {

  @Id
  private String id;

  @Indexed
  private String name;

  private String description;

  private double price;

  /**
   * Example: ["Electronics", "Phone"], used for category-based filters.
   */
  @Indexed
  private List<String> categories;

  /**
   * UI-facing badges/labels like NEW, FREE_SHIPPING, etc.
   */
  @Indexed
  private List<ProductTag> tags;

  @Indexed
  private Instant createdAt;
}