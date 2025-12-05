package com.gdn.training.cart.application.usecase;

import com.gdn.training.cart.application.dto.CartItemResponse;
import com.gdn.training.cart.application.dto.CartResponse;
import com.gdn.training.cart.application.dto.ProductInfoResponse;
import com.gdn.training.cart.application.port.out.CartRepositoryPort;
import com.gdn.training.cart.application.port.out.ProductInfoPort;
import com.gdn.training.cart.domain.model.Cart;
import com.gdn.training.cart.domain.model.CartItem;

import java.util.*;
import java.util.stream.Collectors;

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

        // fecth product info
        Map<UUID, ProductInfoResponse> productInfoMap = Map.of();
        if (productInfoPort != null && !productIds.isEmpty()) {
            productInfoMap = productInfoPort.fetchProductInfo(productIds);
        }

        List<CartItemResponse> cartItemResponses = cart.getItems()
                .stream()
                .map(ci -> new CartItemResponse(
                        ci.getId(),
                        ci.getProductId(),
                        ci.getQuantity(),
                        productInfoMap.get(ci.getProductId())))
                .toList();
        return new CartResponse(
                cart.getId(),
                cart.getMemberId(),
                cart.getCreatedAt(),
                cart.getUpdatedAt(),
                cartItemResponses);
    }

    private CartItemResponse mapItem(CartItem cartItem) {
        return new CartItemResponse(
                cartItem.getId(),
                cartItem.getProductId(),
                cartItem.getQuantity());
    }
}
