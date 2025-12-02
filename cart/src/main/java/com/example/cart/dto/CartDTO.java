package com.example.cart.dto;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {
    private ObjectId id;

    @Positive(message = "total price cannot be negative")
    private Double totalPrice;
    List<ProductDTO> cartItems;
}
