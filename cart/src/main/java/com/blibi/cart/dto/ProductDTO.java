package com.blibi.cart.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * DTO matching the actual Product service response structure
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ProductDTO {

    private String productId;
    private String productName;
    private String description;

    @JsonProperty("price")
    private Double price;

    private String category;
    private List<String> images;

    /**
     * Helper method to get price as productUnitPrice for compatibility
     */
    public double getProductUnitPrice() {
        return price != null ? price : 0.0;
    }
}
