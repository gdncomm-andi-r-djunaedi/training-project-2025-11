package com.gdn.faurihakim.cart.command.model;

import com.gdn.faurihakim.cart.validation.CartData;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddProductToCartCommandRequest implements CartData {
    @NotBlank(message = "NotBlank")
    private String memberId;

    @NotEmpty(message = "NotEmpty")
    private List<ProductItem> products;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductItem {
        @NotBlank(message = "NotBlank")
        private String productId;
        @NotNull(message = "NotNull")
        private Integer quantity;
    }
}
