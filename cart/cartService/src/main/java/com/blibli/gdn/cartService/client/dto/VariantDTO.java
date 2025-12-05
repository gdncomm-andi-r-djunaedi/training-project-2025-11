package com.blibli.gdn.cartService.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariantDTO {
    private String sku;
    private String color;
    private String size;
    private BigDecimal price;
    private Integer stock;
    private Map<String, Object> attributes;
}
