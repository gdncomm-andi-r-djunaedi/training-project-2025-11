package com.marketplace.cart.dto.request;

import com.marketplace.cart.constant.CartConstants;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request to add an item to cart.
 * Only productId and quantity are required - product details are fetched from
 * product service.
 */
@Data
public class AddToCartRequest {
    @NotBlank(message = CartConstants.ValidationMessages.PRODUCT_ID_REQUIRED)
    private String productId;

    @Min(value = 1, message = CartConstants.ValidationMessages.QUANTITY_MIN)
    private Integer quantity;
}

