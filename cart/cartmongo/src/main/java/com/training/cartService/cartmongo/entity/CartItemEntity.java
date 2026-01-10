package com.training.cartService.cartmongo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;

@Document(collection = Cart.COLLECTION_NAME)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CartItemEntity implements Serializable {

    public static final String COLLECTION_NAME = "cartItems";

    @Id
    private String id;
    private String name;
    private String sku;
    private String description;
    private Double price;
    private String category;
    private List<String> tags;
    private List<String> images;
    private int quantity;
}
