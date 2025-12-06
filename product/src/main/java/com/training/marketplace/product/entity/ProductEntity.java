package com.training.marketplace.product.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "product")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "product_id", nullable = false)
    @NotBlank
    private String productId;

    @Column(name = "product_name", nullable = false)
    @NotBlank
    private String productName;

    @Column(name = "product_price", nullable = false)
    @NotBlank
    private BigDecimal productPrice;

    @Column(name = "product_detail", nullable = false)
    @NotBlank
    private String productDetail;

    @Column(name = "product_notes", nullable = false)
    @NotBlank
    private String productNotes;

    @Column(name = "product_image", nullable = false)
    @NotBlank
    private String productImage;
}
