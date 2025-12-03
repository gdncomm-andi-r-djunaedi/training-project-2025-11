package com.blublu.cart.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("cart")
public class CartDocument {
  @Id
  private String id;
  private String username;
  private List<Item> items;

  @Data
  public static class Item {
    private String skuCode;
    private int quantity;
  }
}
