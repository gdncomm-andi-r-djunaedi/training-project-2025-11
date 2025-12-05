package com.gdn.faurihakim.cart.web.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddProductToCartWebRequest {
    private List<ProductItem> products;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductItem {
        private String productId;
        private Integer quantity;
    }
}
