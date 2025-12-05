package com.gdn.faurihakim.product.command.model;

import com.gdn.faurihakim.product.validation.ProductData;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateProductCommandRequest implements ProductData {
    private String productId;
    @NotBlank(message = "NotBlank")
    private String productName;
    private String description;
    @NotNull(message = "NotNull")
    private Double price;
}
