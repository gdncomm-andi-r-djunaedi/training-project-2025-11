package com.marketplace.cart.dto.request;

import com.marketplace.cart.dto.AddToCartRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddToCartCommandRequest {
    private UUID userId;
    private AddToCartRequest addToCartRequest;
}
