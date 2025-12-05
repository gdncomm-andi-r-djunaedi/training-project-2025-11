package com.sc.cartservice.service;

import com.sc.cartservice.client.ProductFeign;
import com.sc.cartservice.dto.CartItemDto;
import com.sc.cartservice.dto.CartResponseDto;
import com.sc.cartservice.model.Cart;
import com.sc.cartservice.model.CartItem;
import com.sc.cartservice.repository.CartItemRepository;
import com.sc.cartservice.repository.CartRepository;
import com.sc.productservice.dto.ProductDto;
import com.sc.utilsservice.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductFeign productFeign;

    public ApiResponse<String> createCart(String memberId) {
        if (Objects.isNull(memberId)) {
            return ApiResponse.failure("INVALID_CREDENTIALS", "Member must be logged in.");
        }

        Cart cart = new Cart();
        cart.setMemberId(memberId);
        cart.setCartItems(new ArrayList<>());
        cart.setTotalPrice(0.0);

        cartRepository.save(cart);
        return ApiResponse.success("Cart created successfully for member: " + memberId);
    }

    public ApiResponse<CartResponseDto> addToCart(String memberId, String productCode) {
        if (Objects.isNull(memberId)) {
            return ApiResponse.failure("INVALID_CREDENTIALS", "Member must be logged in.");
        }

        ResponseEntity<ApiResponse<ProductDto>> productResponse = productFeign.getProduct(productCode);
        if (!Objects.requireNonNull(productResponse.getBody()).isSuccess()) {
            return ApiResponse.failure("PRODUCT_NOT_FOUND", "Product not found with code: " + productCode);
        }
        Cart cart = cartRepository.findByMemberId(memberId);

        List<CartItem> cartItems = cart.getCartItems();

        ProductDto product = productResponse.getBody().getData();

        CartItem cartItem = cart.getCartItems().stream().filter(item -> item.getProductCode().equals(productCode)).findFirst().orElse(new CartItem());
        cartItem.setProductCode(product.getProductCode());
        cartItem.setProductName(product.getProductName());
        cartItem.setQuantity(cartItem.getQuantity() + 1);
        cartItem.setProductPrice(product.getProductPrice() * cartItem.getQuantity());
        if (!cartItems.contains(cartItem)) {
            cartItems.add(cartItem);
        }
        cart.setCartItems(cartItems);
        cart.setTotalPrice(cart.getTotalPrice() + product.getProductPrice());


        cartItemRepository.save(cartItem);
        Cart savedCart = cartRepository.save(cart);


        return ApiResponse.success(toResponseDto(savedCart));

    }


    public ApiResponse<CartResponseDto> getCart(String memberId) {
        if (Objects.isNull(memberId)) {
            return ApiResponse.failure("INVALID_CREDENTIALS", "Member must be logged in.");
        }
        Cart cart = cartRepository.findByMemberId(memberId);
        return ApiResponse.success(toResponseDto(cart));
    }

    public ApiResponse<CartResponseDto> removeFromCart(String memberId, String productCode) {
        if (Objects.isNull(memberId)) {
            return ApiResponse.failure("INVALID_CREDENTIALS", "Member must be logged in.");
        }

        Cart cart = cartRepository.findByMemberId(memberId);
        if (cart == null) {
            return ApiResponse.failure("INVALID_MEMBER", "Cart not found for member id: " + memberId);
        }


        List<CartItem> cartItems = cart.getCartItems();
        CartItem itemToRemove = null;

        for (CartItem item : cartItems) {
            if (item.getProductCode().equals(productCode)) {
                itemToRemove = item;
                break;
            }
        }

        if (itemToRemove != null) {
            cartItems.remove(itemToRemove);
            cart.setTotalPrice(cart.getTotalPrice() - (itemToRemove.getProductPrice() * itemToRemove.getQuantity()));
            cartItemRepository.delete(itemToRemove);
            Cart savedCart = cartRepository.save(cart);
            return ApiResponse.success(toResponseDto(savedCart));
        } else {
            return ApiResponse.failure("ITEM_NOT_FOUND", "Item not found in cart with product code: " + productCode);
        }
    }

    public ApiResponse<CartResponseDto> updateCartItemQuantity(String memberId, String productCode, int deltaQuantity) {
        if (Objects.isNull(memberId)) {
            return ApiResponse.failure("INVALID_CREDENTIALS", "Member must be logged in.");
        }

        Cart cart = cartRepository.findByMemberId(memberId);

        if (cart == null) {
            return ApiResponse.failure("INVALID_MEMBER", "Cart not found for member: " + memberId);
        }

        List<CartItem> cartItems = cart.getCartItems();
        CartItem itemToUpdate = null;

        for (CartItem item : cartItems) {
            if (item.getProductCode().equals(productCode)) {
                itemToUpdate = item;
                break;
            }
        }

        if (itemToUpdate != null) {
            int newQuantity = itemToUpdate.getQuantity() + deltaQuantity;
            if (newQuantity <= 0) {
                return removeFromCart(memberId, productCode);
            }

            double priceDifference = itemToUpdate.getProductPrice() * deltaQuantity;
            itemToUpdate.setQuantity(newQuantity);
            cart.setTotalPrice(cart.getTotalPrice() + priceDifference);

            Cart savedCart = cartRepository.save(cart);
            return ApiResponse.success(toResponseDto(savedCart));
        } else {
            return ApiResponse.failure("ITEM_NOT_FOUND", "Item not found in cart with product code: " + productCode);
        }
    }

    private CartResponseDto toResponseDto(Cart cart) {
        CartResponseDto dto = new CartResponseDto();
        dto.setMemberId(cart.getMemberId());
        dto.setTotalPrice(cart.getTotalPrice());

        List<CartItemDto> items = cart.getCartItems().stream()
                .map(item -> {
                    CartItemDto itemDto = new CartItemDto();
                    BeanUtils.copyProperties(item, itemDto);
                    return itemDto;
                })
                .toList();

        dto.setCartItemDtos(items);
        return dto;
    }
}
