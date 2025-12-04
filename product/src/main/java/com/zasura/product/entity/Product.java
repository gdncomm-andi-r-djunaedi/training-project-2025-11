package com.zasura.product.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "products")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {
  @Id
  @JsonSerialize(using = ToStringSerializer.class)
  private ObjectId id;
  @NotBlank
  @NotNull
  private String name;
  private String description;
  @NotNull
  @DecimalMin("0.0")
  private Double price;

  @CreatedDate
  private LocalDateTime createdDate;

  @LastModifiedDate
  private LocalDateTime lastModifiedDate;

  public Product(String name, String description, double price) {
    this.name = name;
    this.description = description;
    this.price = price;
  }
}