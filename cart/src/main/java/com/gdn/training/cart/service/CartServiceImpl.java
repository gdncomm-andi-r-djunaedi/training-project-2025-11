package com.gdn.training.cart.service;

import com.gdn.training.cart.dto.AddToCartRequest;
import com.gdn.training.cart.dto.CartItemResponse;
import com.gdn.training.cart.dto.CartResponse;
import com.gdn.training.cart.dto.ProductDetailResponse;
import com.gdn.training.cart.entity.Cart;
import com.gdn.training.cart.entity.CartItem;
import com.gdn.training.cart.repository.CartRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final RestTemplate restTemplate;

    @Value("${gdn.product.url}")
    private String productBaseUrl;

    public CartServiceImpl(CartRepository cartRepository, RestTemplate restTemplate) {
        this.cartRepository = cartRepository;
        this.restTemplate = restTemplate;
    }

    @Override
    public CartResponse addToCart(String username, AddToCartRequest request) {
        String productUrl = productBaseUrl + "/api/products/product-detail?product_id=" + request.getProductId();
        ResponseEntity<ProductDetailResponse> response;
        try {
            response = restTemplate.getForEntity(productUrl, ProductDetailResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Product not found or service unavailable: " + e.getMessage());
        }

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Product not found");
        }

        ProductDetailResponse productData = response.getBody();

        Cart cart = cartRepository.findByMemberId(username)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setMemberId(username);
                    return cartRepository.save(newCart);
                });

        Optional<CartItem> existingItem = cart.getCartItems().stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + request.getQuantity();
            if (newQuantity <= 0) {
                cart.getCartItems().remove(item);
            } else {
                item.setQuantity(newQuantity);
            }
        } else {
            if (request.getQuantity() > 0) {
                CartItem newItem = new CartItem();
                newItem.setProductId(request.getProductId());
                newItem.setProductName(productData.getProductName());
                newItem.setPrice(productData.getPrice());
                newItem.setQuantity(request.getQuantity());
                newItem.setCart(cart);
                cart.getCartItems().add(newItem);
            }
        }

        Cart savedCart = cartRepository.save(cart);
        return mapToCartResponse(savedCart);
    }

    @Override
    public CartResponse viewCart(String username) {
        Cart cart = cartRepository.findByMemberId(username).orElse(null);
        if (cart == null) {
            CartResponse response = new CartResponse();
            response.setMemberId(username);
            response.setCartItems(new ArrayList<>());
            return response;
        }

        return mapToCartResponse(cart);
    }

    @Override
    public CartResponse deleteProductFromCart(String username, String productId) {
        Cart cart = cartRepository.findByMemberId(username)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + username));

        Optional<CartItem> itemToDelete = cart.getCartItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();

        if (itemToDelete.isPresent()) {
            cart.getCartItems().remove(itemToDelete.get());
            Cart savedCart = cartRepository.save(cart);
            return mapToCartResponse(savedCart);
        } else {
            return mapToCartResponse(cart);
        }
    }

    private CartResponse mapToCartResponse(Cart cart) {
        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        response.setMemberId(cart.getMemberId());

        List<CartItemResponse> itemResponses = new ArrayList<>();
        for (CartItem item : cart.getCartItems()) {
            CartItemResponse itemResponse = new CartItemResponse();
            itemResponse.setId(item.getId() != null ? item.getId().toString() : null);
            itemResponse.setProductId(item.getProductId());
            itemResponse.setProductName(item.getProductName());
            itemResponse.setPrice(item.getPrice());
            itemResponse.setQuantity(item.getQuantity());
            itemResponses.add(itemResponse);
        }
        response.setCartItems(itemResponses);

        return response;
    }
}
