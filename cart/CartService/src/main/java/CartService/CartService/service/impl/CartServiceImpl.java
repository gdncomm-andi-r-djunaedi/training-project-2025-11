package CartService.CartService.service.impl;

import CartService.CartService.common.ApiResponse;
import CartService.CartService.dto.CartItemResponseDto;
import CartService.CartService.dto.CartRequestDto;
import CartService.CartService.dto.CartResponseDto;
import CartService.CartService.dto.ProductClientResponse;
import CartService.CartService.entity.Cart;
import CartService.CartService.entity.CartItem;
import CartService.CartService.repository.CartRepository;
import CartService.CartService.service.CartService;
import CartService.CartService.service.client.ProductClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    ProductClient productClient;

    @Autowired
    CartRepository cartRepository;

    @Override
    public CartResponseDto addToCart(String userId, CartRequestDto request) {
        ApiResponse<ProductClientResponse> productResponse = productClient.validateProduct(request.getProductId());
        if (productResponse.getData() == null) {
            throw new RuntimeException("Product not found with id: " + request.getProductId());
        }

        Cart cart = cartRepository.findById(userId)
                .orElse(new Cart(userId, new ArrayList<>()));

        boolean exists = false;
        for (CartItem item : cart.getItems()) {
            if (item.getProductId().equals(request.getProductId())) {
                item.setQuantity(item.getQuantity() + request.getQuantity());
                exists = true;
                break;
            }
        }

        if (!exists) {
            cart.getItems().add(new CartItem(request.getProductId(), request.getQuantity()));
        }
        cartRepository.save(cart);
        return toDto(cart);
    }

    @Override
    public CartResponseDto viewCart(String userId) {
        Cart cart = cartRepository.findById(userId)
                .orElse(new Cart(userId, new ArrayList<>()));

        boolean removed = cart.getItems().removeIf(item ->
                productClient.validateProduct(item.getProductId()).getData() == null
        );
        if (removed) {
            cartRepository.save(cart);
        }

        List<CartItemResponseDto> itemResponses = cart.getItems().stream()
                .map(this::mapToCartItemResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new CartResponseDto(userId, itemResponses);
    }


    @Override
    public CartResponseDto removeItem(String userId, String productId) {
        Cart cart = cartRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));

        cart.getItems().removeIf(item -> item.getProductId().equals(productId));
        cartRepository.save(cart);

        List<CartItemResponseDto> itemResponses = cart.getItems().stream()
                .map(this::mapToCartItemResponse)
                .collect(Collectors.toList());

        return new CartResponseDto(userId, itemResponses);
    }

    @Override
    public CartResponseDto clearCart(String userId) {
        Cart cart = cartRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));

        cart.getItems().clear();
        cartRepository.save(cart);

        return new CartResponseDto(userId, new ArrayList<>());
    }

    private CartItemResponseDto mapToCartItemResponse(CartItem item) {
        ApiResponse<ProductClientResponse> response = productClient.validateProduct(item.getProductId());
        ProductClientResponse product = response.getData(); // Get full product details
        if (product == null) {
            return null;
        }
        return new CartItemResponseDto(
                item.getProductId(),
                product.getName(),
                product.getPrice(),
                item.getQuantity()
        );
    }

    private CartResponseDto toDto(Cart cart) {
        CartResponseDto dto = new CartResponseDto();
        dto.setUserId(cart.getUserId());
        dto.setItems(cart.getItems());
        return dto;
    }
}
