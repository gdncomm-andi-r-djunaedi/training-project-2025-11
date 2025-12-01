package com.example.product.domain;

import com.example.product.model.ProductTag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
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

  private String name;
  private String description;
  private double price;

  private List<String> categories;
  private List<ProductTag> tags;
  private Instant createdAt;
}