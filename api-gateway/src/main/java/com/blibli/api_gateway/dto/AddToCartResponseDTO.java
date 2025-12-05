package com.blibli.api_gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddToCartResponseDTO {
    private String userEmail;
    private Double totalPrice;
    private List<ProductsDTO> productsDTOList = new ArrayList<>();
}
