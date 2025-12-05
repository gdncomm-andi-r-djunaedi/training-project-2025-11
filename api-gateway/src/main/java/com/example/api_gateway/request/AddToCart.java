package com.example.api_gateway.request;

import jakarta.validation.constraints.Negative;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddToCart {

    @NotBlank(message = "ProductId should be filled")
    private String productId;
    @Positive(message = "product quantity should be positive")
    private Integer productQuantity;
}
