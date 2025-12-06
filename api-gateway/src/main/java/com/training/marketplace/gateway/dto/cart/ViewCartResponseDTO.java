package com.training.marketplace.gateway.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViewCartResponseDTO {
    private String userId;
    private List<ProductCartDTO> products;
}
