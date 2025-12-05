package com.gdn.training.cart_service.service;

import com.gdn.training.cart_service.dto.AddToCartRequest;
import com.gdn.training.cart_service.dto.CartItemResponse;
import com.gdn.training.cart_service.dto.CartResponse;
import com.gdn.training.cart_service.entity.Cart;
import com.gdn.training.cart_service.entity.CartItem;
import com.gdn.training.cart_service.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;

    public CartResponse addToCart(Long memberId, AddToCartRequest request){
        //Create or find cart for member
        Cart cart = cartRepository.findByMemberId(memberId)
                .orElse(Cart.builder()
                        .memberId(memberId)
                        .items(new ArrayList<>())
                        .createdAt(LocalDateTime.now())
                        .build());


        //check if product already exist in cart
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst();

        if(existingItem.isPresent()){
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
        } else {
            CartItem newItem = CartItem.builder()
                    .productId(request.getProductId())
                    .productName(request.getProductName())
                    .price(request.getPrice())
                    .quantity(request.getQuantity())
                    .imageUrl(request.getImageUrl())
                    .build();
            cart.getItems().add(newItem);
        }

        //calculate total price
        cart.calculateTotalPrice();
        cart.setUpdatedAt(LocalDateTime.now());

        //save cart
        Cart savedCart = cartRepository.save(cart);

        return toResponse(savedCart);
    }

    //GET CART BY MEMBER ID
    public CartResponse getCart(Long memberId){
        Cart cart = cartRepository.findByMemberId(memberId)
                .orElse(Cart.builder()
                        .memberId(memberId)
                        .items(new ArrayList<>())
                        .totalPrice(BigDecimal.ZERO)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build());

        return toResponse(cart);
    }

    //REMOVE ITEM FROM CART
    public CartResponse removeFromCart(Long memberId, Long productId){
        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
// Remove item
        cart.getItems().removeIf(item -> item.getProductId().equals(productId));

        // Recalculate total
        cart.calculateTotalPrice();
        cart.setUpdatedAt(LocalDateTime.now());

        Cart savedCart = cartRepository.save(cart);
        return toResponse(savedCart);
    }

    //CLEAR ENTIRE CART
    public void clearCart(Long memberId) {
        cartRepository.deleteByMemberId(memberId);
    }
    /**
     * Convert Cart entity to CartResponse DTO
     */
    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(item -> CartItemResponse.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .price(item.getPrice())
                        .quantity(item.getQuantity())
                        .subtotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .imageUrl(item.getImageUrl())
                        .build())
                .collect(Collectors.toList());

        return CartResponse.builder()
                .id(cart.getId())
                .memberId(cart.getMemberId())
                .items(itemResponses)
                .totalPrice(cart.getTotalPrice())
                .totalItems(cart.getItems().size())
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }
}
