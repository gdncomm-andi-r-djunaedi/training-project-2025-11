package com.gdn.training.cart.service;

import com.gdn.training.cart.dto.AddToCartRequest;
import com.gdn.training.cart.dto.CartItemResponse;
import com.gdn.training.cart.dto.CartResponse;
import com.gdn.training.cart.entity.Cart;
import com.gdn.training.cart.entity.CartItem;
import com.gdn.training.cart.repository.CartRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final RestTemplate restTemplate;

    public CartServiceImpl(CartRepository cartRepository, RestTemplate restTemplate) {
        this.cartRepository = cartRepository;
        this.restTemplate = restTemplate;
    }

    @Override
    public Cart addToCart(String username, AddToCartRequest request) {
        // 1. Validate Product
        String productUrl = "http://localhost:8081/api/products/product-detail?product_id=" + request.getProductId();
        ResponseEntity<Map> response;
        try {
            response = restTemplate.getForEntity(productUrl, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Product not found or service unavailable: " + e.getMessage());
        }

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Product not found");
        }

        Map<String, Object> productData = response.getBody();
        String productName = (String) productData.get("product_name");

        BigDecimal price;
        Object priceObj = productData.get("price");
        if (priceObj instanceof Integer) {
            price = BigDecimal.valueOf((Integer) priceObj);
        } else if (priceObj instanceof Double) {
            price = BigDecimal.valueOf((Double) priceObj);
        } else {
            price = new BigDecimal(priceObj.toString());
        }

        // 2. Get or Create Cart
        Cart cart = cartRepository.findByMemberId(username)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setMemberId(username);
                    return cartRepository.save(newCart);
                });

        // 3. Add or Update Item
        Optional<CartItem> existingItem = cart.getCartItems().stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
        } else {
            CartItem newItem = new CartItem();
            newItem.setProductId(request.getProductId());
            newItem.setProductName(productName);
            newItem.setPrice(price);
            newItem.setQuantity(request.getQuantity());
            newItem.setCart(cart);
            cart.getCartItems().add(newItem);
        }

        return cartRepository.save(cart);
    }

    @Override
    public CartResponse viewCart(String username) {
        Cart cart = cartRepository.findByMemberId(username).orElse(null);
        if (cart == null) {
            CartResponse response = new CartResponse();
            response.setMemberId(username);
            response.setCartItems(new java.util.ArrayList<>());
            return response;
        }

        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        response.setMemberId(cart.getMemberId());

        java.util.List<CartItemResponse> itemResponses = new java.util.ArrayList<>();
        for (CartItem item : cart.getCartItems()) {
            CartItemResponse itemResponse = new CartItemResponse();
            itemResponse.setId(item.getId().toString());
            itemResponse.setProductId(item.getProductId());
            itemResponse.setProductName(item.getProductName());
            itemResponse.setPrice(item.getPrice());
            itemResponse.setQuantity(item.getQuantity());
            itemResponses.add(itemResponse);
        }
        response.setCartItems(itemResponses);

        return response;
    }

    @Override
    public Cart deleteProductFromCart(String username, String productId) {
        Cart cart = cartRepository.findByMemberId(username)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + username));

        Optional<CartItem> itemToDelete = cart.getCartItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();

        if (itemToDelete.isPresent()) {
            cart.getCartItems().remove(itemToDelete.get());
            return cartRepository.save(cart);
        } else {
            return cart;
        }
    }
}
