package com.blublu.cart.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class CartResponse {
  private List<ItemResponse> items;
  private String username;


  @Builder
  @Data
  public static class ItemResponse {
    private String id;
    private int quantity;
    private String skuCode;
    private String name;
    private String description;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal price;
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal originalPrice;

    private List<String> categories;
  }
}
