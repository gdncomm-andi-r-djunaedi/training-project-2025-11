package org.edmund.cart.serviceimpl;

import lombok.RequiredArgsConstructor;
import org.edmund.cart.response.CartItemResponse;
import org.edmund.cart.response.CartResponse;
import org.edmund.cart.services.CartService;
import org.edmund.cart.services.ProductService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final StringRedisTemplate redisTemplate;
    private final ProductService productService;

    private String cartKey(Long userId) {
        return "cart:" + userId;
    }

    @Override
    public void addItem(Long userId, String productSku, int qty) {
        if (!productService.exists(productSku)) {
            throw new IllegalStateException("Product not found with SKU: " + productSku);
        }
        redisTemplate.opsForHash().increment(cartKey(userId), productSku, qty);
    }

    @Override
    public CartResponse getCart(Long userId) {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(cartKey(userId));

        List<CartItemResponse> items = entries.entrySet()
                .stream()
                .map(e -> new CartItemResponse((String) e.getKey(), Integer.parseInt(e.getValue().toString())))
                .toList();

        int totalItems = items.stream().mapToInt(CartItemResponse::getQuantity).sum();

        return new CartResponse(userId, items, totalItems);
    }
}
