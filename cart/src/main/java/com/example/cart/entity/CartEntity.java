package com.example.cart.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Table(name = "cart")
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class CartEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private UUID customerId;
    private String productId;
    private String customerName;
    private Integer productQuantity;
    private Double itemPrice;
    private Double totalPrice;
    private String productName;
}
