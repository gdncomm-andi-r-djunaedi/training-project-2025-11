package com.training.marketplace.cart.entity;

import java.util.List;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Document(collection = "cart")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartEntity {
    
    @Id
    private UUID id;

    @Field(name = "user_id")
    private String userId;

    @Field(name = "cart_products")
    private List<ProductEntity> cartProducts;
}
