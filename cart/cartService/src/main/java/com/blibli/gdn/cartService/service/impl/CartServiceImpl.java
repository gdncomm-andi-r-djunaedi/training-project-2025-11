package com.blibli.gdn.cartService.service.impl;

import com.blibli.gdn.cartService.client.ProductServiceClient;
import com.blibli.gdn.cartService.client.dto.ProductDTO;
import com.blibli.gdn.cartService.client.dto.VariantDTO;
import com.blibli.gdn.cartService.exception.CartNotFoundException;
import com.blibli.gdn.cartService.exception.InvalidQuantityException;
import com.blibli.gdn.cartService.exception.ItemNotFoundInCartException;
import com.blibli.gdn.cartService.exception.ProductNotFoundException;
import com.blibli.gdn.cartService.model.Cart;
import com.blibli.gdn.cartService.model.CartItem;
import com.blibli.gdn.cartService.repository.CartRepository;
import com.blibli.gdn.cartService.service.CartService;
import com.blibli.gdn.cartService.web.model.AddToCartRequest;
import com.blibli.gdn.cartService.web.model.UpdateQuantityRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductServiceClient productServiceClient;

    private static final int CART_EXPIRY_DAYS = 30;

    @Override
    public Cart addToCart(String memberId, AddToCartRequest request) {
        log.info("Adding item to cart for member: {}, sku: {}", memberId, request.getSku());

        if (request.getQty() == null || request.getQty() < 1) {
            throw new InvalidQuantityException(request.getQty());
        }

        ProductDTO product = productServiceClient.getProductBySku(request.getSku());
        VariantDTO variant = productServiceClient.getVariantBySku(product, request.getSku());

        // Get or create cart
        Cart cart = cartRepository.findByMemberId(memberId)
                .orElse(Cart.builder()
                        .memberId(memberId)
                        .items(new ArrayList<>())
                        .currency("USD")
                        .build());

        // Check if item already exists
        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getSku().equals(request.getSku()))
                .findFirst()
                .orElse(null);

        CartItem cartItem;
        if (existingItem != null) {
            // Update quantity
            existingItem.setQty(existingItem.getQty() + request.getQty());
            cartItem = existingItem;
            log.info("Updated existing item quantity: {}", existingItem.getQty());
        } else {
            // Create new item
            Map<String, Object> variantMap = new HashMap<>();
            if (variant.getColor() != null) variantMap.put("color", variant.getColor());
            if (variant.getSize() != null) variantMap.put("size", variant.getSize());

            cartItem = CartItem.builder()
                    .sku(request.getSku())
                    .productId(product.getProductId())
                    .name(product.getName())
                    .price(variant.getPrice())
                    .currency("USD")
                    .qty(request.getQty())
                    .addedAt(Instant.now())
                    .variant(variantMap)
                    .build();

            cart.getItems().add(cartItem);
            log.info("Added new item to cart");
        }

        // Recalculate totals
        recalculateTotals(cart);

        // Set expiry
        cart.setUpdatedAt(Instant.now());
        cart.setExpireAt(Instant.now().plus(CART_EXPIRY_DAYS, ChronoUnit.DAYS));

        // Save cart
        Cart savedCart = cartRepository.save(cart);

        log.info("Cart updated successfully for member: {}", memberId);
        return savedCart;
    }

    @Override
    public Cart getCart(String memberId) {
        log.debug("Fetching cart for member: {}", memberId);
        Cart cart = cartRepository.findByMemberId(memberId)
                .orElse(Cart.builder()
                        .memberId(memberId)
                        .items(new ArrayList<>())
                        .totalItems(0)
                        .totalValue(BigDecimal.ZERO)
                        .currency("USD")
                        .build());

        if (cart.getItems() != null && !cart.getItems().isEmpty()) {
            boolean isUpdated = false;
            for (CartItem item : cart.getItems()) {
                try {
                    ProductDTO product = productServiceClient.getProductBySku(item.getSku());
                    VariantDTO variant = productServiceClient.getVariantBySku(product, item.getSku());

                    // Check for price change
                    if (variant.getPrice().compareTo(item.getPrice()) != 0) {
                        log.info("Price changed for SKU {}: old={}, new={}", item.getSku(), item.getPrice(), variant.getPrice());
                        item.setPrice(variant.getPrice());
                        isUpdated = true;
                    }

                    // Check for name change (optional, but good for consistency)
                    if (!product.getName().equals(item.getName())) {
                        item.setName(product.getName());
                        isUpdated = true;
                    }

                } catch (Exception e) {
                    log.error("Failed to sync item {} with Product Service: {}", item.getSku(), e.getMessage());
                    // Continue with existing item data if sync fails
                }
            }

            if (isUpdated) {
                recalculateTotals(cart);
                cart.setUpdatedAt(Instant.now());
                cartRepository.save(cart);
                log.info("Cart synced with Product Service and updated for member: {}", memberId);
            }
        }

        return cart;
    }

    @Override
    public Cart updateQuantity(String memberId, String sku, UpdateQuantityRequest request) {
        log.info("Updating quantity for member: {}, sku: {}, qty: {}", memberId, sku, request.getQty());

        if (request.getQty() == null || request.getQty() < 1) {
            throw new InvalidQuantityException(request.getQty());
        }

        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CartNotFoundException(memberId));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getSku().equals(sku))
                .findFirst()
                .orElseThrow(() -> new ItemNotFoundInCartException(sku));

        item.setQty(request.getQty());

        recalculateTotals(cart);
        cart.setUpdatedAt(Instant.now());
        cart.setExpireAt(Instant.now().plus(CART_EXPIRY_DAYS, ChronoUnit.DAYS));

        Cart savedCart = cartRepository.save(cart);

        log.info("Quantity updated successfully");
        return savedCart;
    }

    @Override
    public void removeItem(String memberId, String sku) {
        log.info("Removing item from cart for member: {}, sku: {}", memberId, sku);

        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CartNotFoundException(memberId));

        boolean removed = cart.getItems().removeIf(item -> item.getSku().equals(sku));

        if (!removed) {
            log.info("Item already removed from cart");
        }

        recalculateTotals(cart);
        cart.setUpdatedAt(Instant.now());

        cartRepository.save(cart);

        log.info("Item removed successfully");
    }

    @Override

    public void clearCart(String memberId) {
        log.info("Clearing cart for member: {}", memberId);
        cartRepository.deleteByMemberId(memberId);
        log.info("Cart cleared successfully");
    }

    @Override
    public void mergeCarts(String guestCartId, String memberId) {
        log.info("Merging guest cart {} into member cart {}", guestCartId, memberId);

        cartRepository.findByMemberId(guestCartId).ifPresent(guestCart -> {
            Cart memberCart = cartRepository.findByMemberId(memberId)
                    .orElse(Cart.builder()
                            .memberId(memberId)
                            .items(new ArrayList<>())
                            .currency("USD")
                            .build());

            if (guestCart.getItems() != null && !guestCart.getItems().isEmpty()) {
                for (CartItem guestItem : guestCart.getItems()) {
                    CartItem existingItem = memberCart.getItems().stream()
                            .filter(item -> item.getSku().equals(guestItem.getSku()))
                            .findFirst()
                            .orElse(null);

                    if (existingItem != null) {
                        existingItem.setQty(existingItem.getQty() + guestItem.getQty());
                    } else {
                        memberCart.getItems().add(guestItem);
                    }
                }

                recalculateTotals(memberCart);
                memberCart.setUpdatedAt(Instant.now());
                memberCart.setExpireAt(Instant.now().plus(CART_EXPIRY_DAYS, ChronoUnit.DAYS));

                cartRepository.save(memberCart);
                log.info("Merged {} items from guest cart to member cart", guestCart.getItems().size());
            }

            cartRepository.deleteByMemberId(guestCartId);
            log.info("Deleted guest cart {}", guestCartId);
        });
    }

    private void recalculateTotals(Cart cart) {
        int totalItems = cart.getItems().stream()
                .mapToInt(CartItem::getQty)
                .sum();

        BigDecimal totalValue = cart.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQty())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        cart.setTotalItems(totalItems);
        cart.setTotalValue(totalValue);

        log.debug("Recalculated totals - items: {}, value: {}", totalItems, totalValue);
    }
}
