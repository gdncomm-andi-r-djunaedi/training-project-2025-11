package com.blibli.gdn.cartService.web.model;

import com.blibli.gdn.cartService.model.Cart;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class CartResponseDTOs {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddToCartResponse {
        private String message;
        private String sku;
        private Integer qty;
        private Cart cart;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateQuantityResponse {
        private String message;
        private String sku;
        private Integer qty;
        private Cart cart;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RemoveItemResponse {
        private String message;
        private String sku;
        private Cart cart;
    }
}
