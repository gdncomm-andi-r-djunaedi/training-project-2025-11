package com.blibli.gdn.cartService.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private String id;
    private String productId;
    private String name;
    private String description;
    private String category;
    private String brand;
    @Builder.Default
    private List<VariantDTO> variants = new java.util.ArrayList<>();
}
