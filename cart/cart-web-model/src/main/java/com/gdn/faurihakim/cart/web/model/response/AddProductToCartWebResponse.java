package com.gdn.faurihakim.cart.web.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddProductToCartWebResponse {
    private String cartId;
    private String memberId;
    private Double totalAmount;
    private List<CartItemResponse> items;
    private Long version;
    private Long createdDate;
    private String createdBy;
    private Long lastModifiedDate;
    private String lastModifiedBy;
}
