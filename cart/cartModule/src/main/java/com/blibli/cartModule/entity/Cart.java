package com.blibli.cartModule.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document(collection = "carts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cart {

  @Id
  private String id;
  @Indexed(unique = true)
  private Long memberId;
  private List<CartItem> items = new ArrayList<>();
  private Date createdAt;
  private Date updatedAt;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CartItem {
    private String productId;
    private Integer quantity;
  }
}

