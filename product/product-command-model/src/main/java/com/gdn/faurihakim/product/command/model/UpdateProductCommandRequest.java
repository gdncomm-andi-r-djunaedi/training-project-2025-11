package com.gdn.faurihakim.product.command.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProductCommandRequest {
    private String productId;
    private String productName;
    private String description;
    private Double price;
}
