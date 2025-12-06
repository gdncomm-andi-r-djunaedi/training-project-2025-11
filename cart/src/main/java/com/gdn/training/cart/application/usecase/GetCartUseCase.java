package com.gdn.training.cart.application.usecase;

import com.gdn.training.cart.application.dto.CartItemResponse;
import com.gdn.training.cart.application.dto.CartResponse;
import com.gdn.training.cart.application.dto.ProductInfoResponse;
import com.gdn.training.cart.application.port.out.CartRepositoryPort;
import com.gdn.training.cart.application.port.out.ProductInfoPort;
import com.gdn.training.cart.domain.model.Cart;
import com.gdn.training.cart.domain.model.CartItem;

import java.util.*;

public class GetCartUseCase {

    private final CartRepositoryPort cartRepository;
    private final ProductInfoPort productInfoPort;

    public GetCartUseCase(CartRepositoryPort cartRepository, ProductInfoPort productInfoPort) {
        this.cartRepository = cartRepository;
        this.productInfoPort = productInfoPort;
    }

    public CartResponse execute(UUID memberId) {

        Optional<Cart> cartOptional = cartRepository.findByMemberId(memberId);
        if (cartOptional.isEmpty()) {
            return CartResponse.emptyForMember(memberId);
        }

        Cart cart = cartOptional.get();

        List<UUID> productIds = cart.getItems()
                .stream()
                .map(CartItem::getProductId)
                .distinct()
                .toList();

        // FIX: productInfoMap must be effectively final
        Map<UUID, ProductInfoResponse> productInfoMap = (productInfoPort != null && !productIds.isEmpty())
                ? productInfoPort.fetchProductInfo(productIds)
                : Collections.emptyMap();

        List<CartItemResponse> cartItemResponses = cart.getItems()
                .stream()
                .map(ci -> new CartItemResponse(
                        ci.getId(),
                        ci.getProductId(),
                        ci.getQuantity(),
                        productInfoMap.get(ci.getProductId()) // ← Now allowed
                ))
                .toList();

        return new CartResponse(
                cart.getId(),
                cart.getMemberId(),
                cart.getCreatedAt(),
                cart.getUpdatedAt(),
                cartItemResponses);
    }
}
