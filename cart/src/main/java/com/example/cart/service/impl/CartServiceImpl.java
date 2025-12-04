package com.example.cart.service.impl;

import com.example.cart.dto.CartDTO;
import com.example.cart.dto.response.GenericResponseSingleDTO;
import com.example.cart.dto.response.ProductServiceResponse;
import com.example.cart.entity.Cart;
import com.example.cart.entity.Product;
import com.example.cart.exception.CartNotFoundException;
import com.example.cart.exception.ProductNotFoundException;
import com.example.cart.feign.ProductFeignClient;
import com.example.cart.repository.CartRepository;
import com.example.cart.service.CartService;
import com.example.cart.utils.DTOUtils;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductFeignClient productFeignClient;
    private static final String CART_CACHE = "cart";

    @Override
    @CachePut(value = CART_CACHE, key = "#cartId.toString()")
    public CartDTO addProductToCart(ObjectId cartId, String productId) {
        log.debug("addProductToCart:: cartId - {}, productId - {}", cartId, productId);
        
        Cart cart = getCartFromCacheOrDB(cartId);
        GenericResponseSingleDTO<ProductServiceResponse> productServiceResponse;

        try {
            productServiceResponse = productFeignClient.getProductById(productId);
            log.info("Status Code - {}", productServiceResponse.getStatusCode().toString());
            log.info("Status Message - {}", productServiceResponse.getStatusMessage());
            log.info("Status Response - {}", productServiceResponse.getResponse());
        } catch (FeignException feignException) {
            throw new ProductNotFoundException(" FAILED - addProductToCart:: cartId - " + cartId + ", productId - " + productId);
        }

        Product productToAdd = DTOUtils.getEntity(productServiceResponse.getResponse());
        // Set default quantity of 1 if quantity is null (product service doesn't provide quantity)
        if (productToAdd.getQuantity() == null) {
            productToAdd.setQuantity(1);
        }
        
        List<Product> cartItems = cart.getCartItems() != null ? cart.getCartItems() : new ArrayList<>();

        // Check if product already exists in cart
        boolean productExists = false;
        for (Product item : cartItems) {
            if (item.getProductId().equals(productToAdd.getProductId())) {
                // Update quantity if product already exists
                item.setQuantity(item.getQuantity() + productToAdd.getQuantity());
                productExists = true;
                log.debug("Product {} already exists in cart, updating quantity to {}", 
                        productToAdd.getProductId(), item.getQuantity());
                break;
            }
        }

        // Add new product if it doesn't exist
        if (!productExists) {
            cartItems.add(productToAdd);
            log.debug("Adding new product {} to cart", productToAdd.getProductId());
        }

        cart.setCartItems(cartItems);
        cart.setTotalPrice(calculateTotalPrice(cartItems));
        
        Cart savedCart = cartRepository.save(cart);
        log.debug("Cart saved successfully with totalPrice: {}", savedCart.getTotalPrice());
        
        return DTOUtils.getDTO(savedCart);
    }

    @Override
    @Cacheable(value = CART_CACHE, key = "#cartId.toString()", unless = "#result == null")
    public CartDTO getCart(ObjectId cartId) {
        log.debug("getCart:: cartId - {}", cartId);
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found with id: " + cartId));
        return DTOUtils.getDTO(cart);
    }

    @Override
    @CachePut(value = CART_CACHE, key = "#cartId.toString()")
    public CartDTO deleteProductFromCart(ObjectId cartId, String productId) {
        log.debug("deleteProductFromCart:: cartId - {}, productId - {}", cartId, productId);
        
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found with id: " + cartId));

        List<Product> cartItems = cart.getCartItems() != null ? cart.getCartItems() : new ArrayList<>();
        
        // Remove product by productId
        boolean removed = cartItems.removeIf(item -> item.getProductId().equals(productId));
        
        if (!removed) {
            log.warn("Product {} not found in cart {}", productId, cartId);
            throw new CartNotFoundException("Product not found in cart with productId: " + productId);
        }

        cart.setCartItems(cartItems);
        cart.setTotalPrice(calculateTotalPrice(cartItems));
        
        Cart savedCart = cartRepository.save(cart);
        log.debug("Product {} removed from cart, new totalPrice: {}", productId, savedCart.getTotalPrice());
        
        return DTOUtils.getDTO(savedCart);
    }

    /**
     * Helper method to get cart from cache or database
     * This avoids duplicate code in addProductToCart and deleteProductFromCart
     */
    private Cart getCartFromCacheOrDB(ObjectId cartId) {
        return cartRepository.findById(cartId)
                .orElseGet(() -> {
                    log.debug("Cart not found, creating new cart with id: {}", cartId);
                    Cart newCart = new Cart();
                    newCart.setId(cartId);
                    newCart.setCartItems(new ArrayList<>());
                    newCart.setTotalPrice(0.0);
                    return newCart;
                });
    }

    private Double calculateTotalPrice(List<Product> cartItems) {
        if (cartItems == null || cartItems.isEmpty()) {
            return 0.0;
        }
        return cartItems.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }
}

