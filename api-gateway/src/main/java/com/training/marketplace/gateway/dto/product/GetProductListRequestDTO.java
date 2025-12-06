package com.training.marketplace.gateway.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetProductListRequestDTO {
    private String query;
    private int page;
    private int itemPerPage;
}
