package com.training.marketplace.gateway.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetProductListResponseDTO {
    private List<ProductListItemDTO> productList;
    private int page;
    private int itemPerPage;
}
