package com.marketplace.product.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ProductIdsRequest {
    @NotEmpty(message = "Product IDs list cannot be empty")
    private List<String> ids;
}
