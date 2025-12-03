package com.microservice.cart.service.impl;

import com.microservice.cart.client.ProductFeign;
import com.microservice.cart.dto.AddToCartRequestDto;
import com.microservice.cart.dto.CartDto;
import com.microservice.cart.dto.CartItemDto;
import com.microservice.cart.dto.ProductResponseDto;
import com.microservice.cart.entity.Cart;
import com.microservice.cart.entity.CartItem;
import com.microservice.cart.exceptions.BusinessException;
import com.microservice.cart.exceptions.ResourceNotFoundException;
import com.microservice.cart.exceptions.ValidationException;
import com.microservice.cart.repository.CartRepository;
import com.microservice.cart.service.CartService;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductFeign productFeign;

    @Override
    public CartDto getCart(Long userId) {
        log.info("Fetching cart for userId: {}", userId);
        
        if (userId == null || userId <= 0) {
            log.warn("Invalid userId provided: {}", userId);
            throw new ValidationException("Invalid userId. UserId must be a positive number.");
        }

        Optional<Cart> cartOptional = cartRepository.findByUserId(userId);
        
        if (!cartOptional.isPresent()) {
            log.info("Cart not found for userId: {}, returning empty cart", userId);
            CartDto emptyCart = new CartDto();
            emptyCart.setUserId(userId);
            emptyCart.setItems(new ArrayList<>());
            emptyCart.setUpdatedAt(new Date());
            return emptyCart;
        }

        Cart cart = cartOptional.get();
        log.info("Cart found for userId: {} with {} items", userId, 
                cart.getItems() != null ? cart.getItems().size() : 0);

        CartDto cartDto = new CartDto();
        cartDto.setUserId(cart.getUserId());
        cartDto.setUpdatedAt(cart.getUpdatedAt());

        List<CartItemDto> cartItemDtos = new ArrayList<>();
        
        if (cart.getItems() != null && !cart.getItems().isEmpty()) {
            for (CartItem cartItem : cart.getItems()) {
                try {
                    log.info("Fetching product details for productId: {}", cartItem.getProductId());
                    ProductResponseDto product = productFeign.getProduct(cartItem.getProductId());
                    
                    CartItemDto cartItemDto = mapToCartItemDto(cartItem, product);
                    cartItemDtos.add(cartItemDto);
                    log.info("Successfully fetched product details for productId: {}", cartItem.getProductId());
                } catch (FeignException.NotFound e) {
                    log.warn("Product not found for productId: {}, skipping item", cartItem.getProductId());
                } catch (FeignException e) {
                    log.error("Error fetching product details for productId: {}, status: {}", 
                            cartItem.getProductId(), e.status(), e);
                    throw new BusinessException("Unable to fetch product details. Please try again later.");
                } catch (Exception e) {
                    log.error("Unexpected error fetching product details for productId: {}", 
                            cartItem.getProductId(), e);
                    throw new BusinessException("An error occurred while fetching product details.");
                }
            }
        }

        cartDto.setItems(cartItemDtos);
        log.info("Successfully retrieved cart for userId: {} with {} items", userId, cartItemDtos.size());
        return cartDto;
    }

    @Override
    public CartDto addItemToCart(Long userId, AddToCartRequestDto request) {
        log.info("Adding item to cart for userId: {}, productId: {}, quantity: {}", 
                userId, request.getProductId(), request.getQuantity());

        if (userId == null || userId <= 0) {
            log.warn("Invalid userId provided: {}", userId);
            throw new ValidationException("Invalid userId. UserId must be a positive number.");
        }

        if (request == null) {
            log.warn("AddToCartRequestDto is null");
            throw new ValidationException("Request body cannot be null.");
        }

        if (request.getProductId() == null || request.getProductId() <= 0) {
            log.warn("Invalid productId provided: {}", request.getProductId());
            throw new ValidationException("Invalid productId. ProductId must be a positive number.");
        }

        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            log.warn("Invalid quantity provided: {}", request.getQuantity());
            throw new ValidationException("Invalid quantity. Quantity must be a positive number.");
        }

        ProductResponseDto product;
        try {
            log.info("Fetching product details for productId: {}", request.getProductId());
            product = productFeign.getProduct(request.getProductId());
            log.info("Successfully fetched product details for productId: {}", request.getProductId());
        } catch (FeignException.NotFound e) {
            log.warn("Product not found for productId: {}", request.getProductId());
            throw new ResourceNotFoundException("Product not found with id: " + request.getProductId());
        } catch (FeignException e) {
            log.error("Error fetching product details for productId: {}, status: {}", 
                    request.getProductId(), e.status(), e);
            throw new BusinessException("Unable to fetch product details. Please try again later.");
        } catch (Exception e) {
            log.error("Unexpected error fetching product details for productId: {}", 
                    request.getProductId(), e);
            throw new BusinessException("An error occurred while fetching product details.");
        }

        Optional<Cart> cartOptional = cartRepository.findByUserId(userId);
        Cart cart;
        
        if (cartOptional.isPresent()) {
            cart = cartOptional.get();
            log.info("Existing cart found for userId: {}", userId);
        } else {
            cart = new Cart();
            cart.setUserId(userId);
            cart.setItems(new ArrayList<>());
            log.info("Creating new cart for userId: {}", userId);
        }

        List<CartItem> items = cart.getItems() != null ? cart.getItems() : new ArrayList<>();
        Optional<CartItem> existingItem = items.stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + request.getQuantity();
            item.setQuantity(newQuantity);
            log.info("Updated quantity for existing productId: {} to {}", request.getProductId(), newQuantity);
        } else {
            CartItem newItem = new CartItem();
            newItem.setProductId(request.getProductId());
            newItem.setQuantity(request.getQuantity());
            newItem.setPrice(product.getPrice());
            newItem.setAddedAt(new Date());
            items.add(newItem);
            log.info("Added new item to cart: productId: {}, quantity: {}", 
                    request.getProductId(), request.getQuantity());
        }

        cart.setItems(items);
        cart.setUpdatedAt(new Date());

        try {
            cart = cartRepository.save(cart);
            log.info("Cart saved successfully for userId: {}", userId);
        } catch (Exception e) {
            log.error("Error saving cart for userId: {}", userId, e);
            throw new BusinessException("Failed to save cart. Please try again.");
        }

        CartDto cartDto = new CartDto();
        cartDto.setUserId(cart.getUserId());
        cartDto.setUpdatedAt(cart.getUpdatedAt());

        List<CartItemDto> cartItemDtos = new ArrayList<>();
        for (CartItem cartItem : cart.getItems()) {
            try {
                ProductResponseDto productForItem = productFeign.getProduct(cartItem.getProductId());
                CartItemDto cartItemDto = mapToCartItemDto(cartItem, productForItem);
                cartItemDtos.add(cartItemDto);
            } catch (Exception e) {
                log.error("Error fetching product details for productId: {} in response building", 
                        cartItem.getProductId(), e);
            }
        }

        cartDto.setItems(cartItemDtos);
        log.info("Successfully added item to cart for userId: {}", userId);
        return cartDto;
    }

    @Override
    public void removeItemFromCart(Long userId, Long itemId) {
        log.info("Removing item from cart for userId: {}, itemId: {}", userId, itemId);

        if (userId == null || userId <= 0) {
            log.warn("Invalid userId provided: {}", userId);
            throw new ValidationException("Invalid userId. UserId must be a positive number.");
        }

        if (itemId == null || itemId <= 0) {
            log.warn("Invalid itemId provided: {}", itemId);
            throw new ValidationException("Invalid itemId. ItemId must be a positive number.");
        }

        Optional<Cart> cartOptional = cartRepository.findByUserId(userId);
        if (!cartOptional.isPresent()) {
            log.warn("Cart not found for userId: {}", userId);
            throw new ResourceNotFoundException("Cart not found for userId: " + userId);
        }

        Cart cart = cartOptional.get();
        List<CartItem> items = cart.getItems() != null ? cart.getItems() : new ArrayList<>();

        boolean itemRemoved = items.removeIf(item -> item.getProductId().equals(itemId));
        
        if (!itemRemoved) {
            log.warn("Item with productId: {} not found in cart for userId: {}", itemId, userId);
            throw new ResourceNotFoundException("Item with productId " + itemId + " not found in cart.");
        }

        cart.setItems(items);
        cart.setUpdatedAt(new Date());

        try {
            cartRepository.save(cart);
            log.info("Successfully removed item with productId: {} from cart for userId: {}", itemId, userId);
        } catch (Exception e) {
            log.error("Error saving cart after item removal for userId: {}", userId, e);
            throw new BusinessException("Failed to remove item from cart. Please try again.");
        }
    }

    private CartItemDto mapToCartItemDto(CartItem cartItem, ProductResponseDto product) {
        CartItemDto dto = new CartItemDto();
        dto.setId(product.getId());
        dto.setProductId(cartItem.getProductId());
        dto.setQuantity(cartItem.getQuantity());
        dto.setPrice(cartItem.getPrice());
        dto.setAddedAt(cartItem.getAddedAt());
        dto.setSku(product.getSku());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setCategory(product.getCategory());
        dto.setBrand(product.getBrand());
        dto.setItemCode(product.getItemCode());
        dto.setLength(product.getLength());
        dto.setHeight(product.getHeight());
        dto.setWidth(product.getWidth());
        dto.setWeight(product.getWeight());
        dto.setDangerousLevel(product.getDangerousLevel());
        return dto;
    }
}
