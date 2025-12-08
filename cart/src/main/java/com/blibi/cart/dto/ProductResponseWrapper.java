package com.blibi.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Wrapper DTO for Product service GenericResponse
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ProductResponseWrapper {
    private boolean success;
    private String message;
    private ProductDTO data;
}
