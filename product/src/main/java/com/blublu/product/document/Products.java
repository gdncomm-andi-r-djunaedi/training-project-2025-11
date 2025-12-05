package com.blublu.product.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("products")
public class Products {

  @Id
  private String id;
  @Indexed(unique = true)
  private String skuCode;
  private String name;
  private String description;

  @Field(targetType = FieldType.DECIMAL128)
  private BigDecimal price;
  @Field(targetType = FieldType.DECIMAL128)
  private BigDecimal originalPrice;

  private List<String> categories;
}
