package com.gdn.training.cart.application.dto;

import java.util.UUID;

public record CartItemResponse(
                UUID id,
                UUID productId,
                int quantity,
                ProductInfoResponse productInfoResponse) {

}
