package com.example.cart.service;

import com.example.cart.dto.CartItemRequest;
import com.example.cart.dto.CartItemResponse;
import com.example.cart.model.CartItem;
import com.example.cart.repository.CartItemRepository;
//import com.example.product.dto.ProductDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartItemRepository cartItemRepository;

//    private final UserRepository userRepository;
//
//    private final ProductRepository productRepository;

    public boolean addToCart(String userId, CartItemRequest request) {

//        Optional<User> userOpt = userRepository.findById(Long.parseLong(userId));
//        Optional<Product> productOpt = productRepository.findById(request.getProductId());
//
//        if(productOpt.isEmpty() || userOpt.isEmpty()) {
//            return false;
//        } if (productOpt.get().getStockQuantity() < request.getQuantity()) {
//            return false;
//        }

        CartItem existingCartItem = cartItemRepository.findByUserIdAndProductId(Long.valueOf(userId), request.getProductId());

        if(existingCartItem != null) {
            existingCartItem.setQuantity(existingCartItem.getQuantity() + request.getQuantity());

            // Subtotal Calculation
            // subtotal = current sub total + (quantity * product price)
//            existingCartItem.setSubTotalPrice(existingCartItem.getSubTotalPrice().add(request.get.multiply(BigDecimal.valueOf(request.getQuantity()))));
            existingCartItem.setSubTotalPrice(BigDecimal.valueOf(1000.00));
            cartItemRepository.save(existingCartItem);
        } else {
            CartItem newCartItem = new CartItem();
            updateCartFromRequest(newCartItem, Long.valueOf(userId), request.getProductId(), request);
            cartItemRepository.save(newCartItem);
        }
        return true;
    }

    public boolean removeItemFromCart(String userId, Long itemId) {
//        Optional<Product> productOpt = productRepository.findById(itemId);
//        Optional<User> userOpt = userRepository.findById(Long.valueOf(userId));
//
//        if(productOpt.isEmpty() || userOpt.isEmpty()) return false;

        if(cartItemRepository.findByUserIdAndProductId(Long.valueOf(userId), itemId) != null) {
            cartItemRepository.deleteByUserIdAndProductId(Long.valueOf(userId), itemId);
            return true;
        }
        return false;
    }

    public List<CartItemResponse> fetchCartByUser(String userId) {
//        if (userRepository.findById(Long.valueOf(userId)).isEmpty()) return  null;

        return cartItemRepository.findAllByUserId(Long.valueOf(userId))
                .stream().map(this::mapToCartItemResponse).collect(Collectors.toList());
    }

    public void clearCart(String userId) {
        cartItemRepository.deleteByUserId(Long.valueOf(userId));
    }

    private void updateCartFromRequest(CartItem cartItem, Long userId, Long productId, CartItemRequest cartItemRequest) {
//        cartItem.setUser(user);
        cartItem.setUserId(userId);
//        cartItem.setProduct(product);
        cartItem.setProductId(productId);
        cartItem.setQuantity(cartItemRequest.getQuantity());

        // Subtotal calculation for new cart
        // subtotal = product price * quantity
//        cartItem.setSubTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(cartItemRequest.getQuantity())));
        cartItem.setSubTotalPrice(BigDecimal.valueOf(1000.00));
    }

    private CartItemResponse mapToCartItemResponse(CartItem cartItem) {
        CartItemResponse cartItemResponse = new CartItemResponse();
//        ProductDTO productDTO = new ProductDTO();

//        productDTO.setName(cartItem.getProduct().getName());
//        productDTO.setName(cartItem.get);
//        productDTO.setPrice(cartItem.getProduct().getPrice());
//        productDTO.setCategory(cartItem.getProduct().getCategory());
//        productDTO.setStockQuantity(cartItem.getProduct().getStockQuantity());
//        productDTO.setDescription(cartItem.getProduct().getDescription());
//        productDTO.setImageUrl(cartItem.getProduct().getImageUrl());

//        cartItemResponse.setProduct(productDTO);
        cartItemResponse.setProductId(cartItem.getProductId().toString());
        cartItemResponse.setSubTotalPrice(cartItem.getSubTotalPrice());
        cartItemResponse.setQuantity(cartItem.getQuantity());
        return cartItemResponse;
    }

}
