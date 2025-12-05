package com.gdn.training.cart.service;

import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.gdn.training.cart.model.entity.CartItem;
import com.gdn.training.cart.model.request.AddToCartRequest;
import com.gdn.training.cart.model.response.CartResponse;
import com.gdn.training.cart.model.dto.ProductDetailDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final RestTemplate restTemplate;

    @Value("${cart.ttl:604800}")
    private long cartTtl;

    private static final String CART_KEY_PREFIX = "cart:";
    private static final String PRODUCT_KEY_SUFFIX = ":product";
    private static final String PRODUCT_SERVICE_URL = "http://localhost:8082/products/detail/";

    public CartResponse addToCart(String userEmail, AddToCartRequest request) {
        String cartKey = CART_KEY_PREFIX + userEmail;
        String productKey = cartKey + PRODUCT_KEY_SUFFIX;

        ProductDetailDTO productDetail = fetchProductDetail(request.getProductId());

        Integer currentQuantity = (Integer) redisTemplate.opsForHash().get(cartKey, request.getProductId());
        if (currentQuantity == null) {
            currentQuantity = 0;
        }

        Integer newQuantity = currentQuantity + request.getQuantity();
        redisTemplate.opsForHash().put(cartKey, request.getProductId(), newQuantity);
        redisTemplate.opsForHash().put(productKey, request.getProductId(), productDetail);

        redisTemplate.expire(cartKey, cartTtl, TimeUnit.SECONDS);
        redisTemplate.expire(productKey, cartTtl, TimeUnit.SECONDS);

        CartResponse response = getCart(userEmail);
        response.setMessage("Product added to cart");
        return response;
    }

    public CartResponse getCart(String userEmail) {
        String cartKey = CART_KEY_PREFIX + userEmail;
        String productKey = cartKey + PRODUCT_KEY_SUFFIX;

        Map<Object, Object> cartItems = redisTemplate.opsForHash().entries(cartKey);

        if (cartItems.isEmpty()) {
            return CartResponse.builder()
                    .message("OK")
                    .userEmail(userEmail)
                    .cartItems(new ArrayList<>())
                    .total(0.0)
                    .itemCount(0)
                    .build();
        }

        Map<Object, Object> productDetails = redisTemplate.opsForHash().entries(productKey);

        List<CartItem> items = new ArrayList<>();
        double totalAmount = 0.0;

        for (Map.Entry<Object, Object> entry : cartItems.entrySet()) {
            String productId = (String) entry.getKey();
            Integer quantity = (Integer) entry.getValue();
            ProductDetailDTO productDetail = (ProductDetailDTO) productDetails.get(productId);

            if (productDetail == null) {
                throw new IllegalArgumentException("Product not found");
            }

            CartItem cartItem = CartItem.builder()
                    .productId(productId)
                    .quantity(quantity)
                    .productName(productDetail.getName())
                    .price(productDetail.getPrice())
                    .build();

            items.add(cartItem);
            totalAmount += cartItem.getSubtotal();
        }

        return CartResponse.builder()
                .message("OK")
                .userEmail(userEmail)
                .cartItems(items)
                .total(totalAmount)
                .itemCount(items.size())
                .build();

    }

    public CartResponse removeFromCart(String userEmail, String productId) {
        String cartKey = CART_KEY_PREFIX + userEmail;
        String productKey = cartKey + PRODUCT_KEY_SUFFIX;

        redisTemplate.opsForHash().delete(cartKey, productId);
        redisTemplate.opsForHash().delete(productKey, productId);

        if (redisTemplate.opsForHash().size(cartKey) == 0) {
            redisTemplate.delete(cartKey);
            redisTemplate.delete(productKey);

            return CartResponse.builder()
                    .userEmail(userEmail)
                    .cartItems(new ArrayList<>())
                    .total(0.0)
                    .itemCount(0)
                    .build();
        }

        CartResponse response = getCart(userEmail);
        response.setMessage("Product removed from cart");
        return response;
    }

    private ProductDetailDTO fetchProductDetail(String productId) {
        try {
            System.out.println(PRODUCT_SERVICE_URL + productId);
            return restTemplate.getForObject(PRODUCT_SERVICE_URL + productId, ProductDetailDTO.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch product detail");
        }
    }

}
