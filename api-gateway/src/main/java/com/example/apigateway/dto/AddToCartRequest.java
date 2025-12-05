package com.example.apigateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for adding a product to the cart")
public class AddToCartRequest {

    @NotNull
    @Schema(description = "Product ID", example = "P-001")
    private String productId;

    @NotNull
    @Schema(description = "Quantity", example = "1")
    private Integer qty;
}