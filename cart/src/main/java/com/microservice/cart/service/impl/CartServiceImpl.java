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
import com.microservice.cart.wrapper.ApiResponse;
import com.microservice.cart.service.CartService;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

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
            emptyCart.setTotalQuantity(0);
            return emptyCart;
        }

        Cart cart = cartOptional.get();
        log.info("Cart found for userId: {} with {} items", userId,
                cart.getItems() != null ? cart.getItems().size() : 0);

        List<String> skuIds = new ArrayList<>();

        if (cart.getItems() != null) {
            for (CartItem cartItem : cart.getItems()) {
                String skuId = cartItem.getSkuId();

                if (skuId != null) {
                    if (!skuIds.contains(skuId)) {
                        skuIds.add(skuId);
                    }
                }
            }
        }

        List<CartItemDto> cartItemDtos = new ArrayList<>();
        List<CartItem> validItems = new ArrayList<>();
        boolean cartNeedsUpdate = false;

        if (!skuIds.isEmpty()) {
            try {
                log.info("Fetching product details for {} SKUs in batch", skuIds.size());
                ApiResponse<List<ProductResponseDto>> productsResponse = productFeign.getProductsBySkuIds(skuIds);
                List<ProductResponseDto> products = productsResponse != null && productsResponse.getData() != null
                        ? productsResponse.getData()
                        : new ArrayList<>();
                Map<String, ProductResponseDto> productMap = new HashMap<>();

                for (ProductResponseDto product : products) {
                    if (product == null) {
                        continue;
                    }

                    String skuId = product.getSkuId();
                    if (!productMap.containsKey(skuId)) {
                        productMap.put(skuId, product);
                    }
                }
                log.info("Successfully fetched {} products", products.size());

                for (CartItem cartItem : cart.getItems()) {
                    ProductResponseDto product = productMap.get(cartItem.getSkuId());
                    if (product != null) {
                        if (!cartItem.getPrice().equals(product.getPrice())) {
                            cartItem.setPrice(product.getPrice());
                            cartNeedsUpdate = true;
                            log.info("Updated price for SkuId: {} from {} to {}",
                                    cartItem.getSkuId(), cartItem.getPrice(), product.getPrice());
                        }

                        validItems.add(cartItem);
                        CartItemDto cartItemDto = mapToCartItemDto(cartItem, product);
                        cartItemDtos.add(cartItemDto);
                    } else {
                        log.warn("Product not found for SkuId: {}, will be removed from cart", cartItem.getSkuId());
                        cartNeedsUpdate = true;
                    }
                }
                if (cartNeedsUpdate) {
                    cart.setItems(validItems);
                    cart.setUpdatedAt(new Date());
                    try {
                        cartRepository.save(cart);
                        log.info("Cart updated with latest prices and removed invalid items for userId: {}. Valid items remaining: {}",
                                userId, validItems.size());

                        if (validItems.isEmpty() && cart.getItems() != null && !cart.getItems().isEmpty()) {
                            log.info("All products were removed from cart for userId: {} as they no longer exist in product service", userId);
                        }
                    } catch (Exception e) {
                        log.error("Error updating cart with latest prices for userId: {}", userId, e);
                    }
                }

            } catch (FeignException e) {
                log.error("Error fetching product details in batch, status: {}", e.status(), e);
                throw new BusinessException("Unable to fetch product details. Please try again later.");
            } catch (Exception e) {
                log.error("Unexpected error fetching product details in batch", e);
                throw new BusinessException("An error occurred while fetching product details.");
            }
        }

        Integer totalQuantity = 0;

        for (CartItemDto cartItemDto : cartItemDtos) {
            Integer itemQuantity = cartItemDto.getQuantity();
            if (itemQuantity != null) {
                totalQuantity = totalQuantity + itemQuantity;
            }
        }

        CartDto cartDto = new CartDto();
        cartDto.setUserId(cart.getUserId());
        cartDto.setItems(cartItemDtos);
        cartDto.setUpdatedAt(cart.getUpdatedAt());
        cartDto.setTotalQuantity(totalQuantity);

        log.info("Successfully retrieved cart for userId: {} with {} items, total quantity: {}",
                userId, cartItemDtos.size(), totalQuantity);
        return cartDto;
    }

    @Override
    public Boolean addItemToCart(Long userId, AddToCartRequestDto request) {
        log.info("Adding item to cart for userId: {}, SkuId: {}, quantity: {}",
                userId, request.getSkuId(), request.getQuantity());

        if (userId == null || userId <= 0) {
            log.warn("Invalid userId provided: {}", userId);
            throw new ValidationException("Invalid userId. UserId must be a positive number.");
        }

        if (request == null) {
            log.warn("AddToCartRequestDto is null");
            throw new ValidationException("Request body cannot be null.");
        }

        if (request.getSkuId() == null || request.getSkuId().trim().isEmpty()) {  // Changed validation
            log.warn("Invalid SkuId provided: {}", request.getSkuId());
            throw new ValidationException("Invalid SkuId. SkuId cannot be null or empty.");
        }

        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            log.warn("Invalid quantity provided: {}", request.getQuantity());
            throw new ValidationException("Invalid quantity. Quantity must be a positive number.");
        }

        Optional<Cart> cartOptional = cartRepository.findByUserId(userId);
        boolean itemExistsInCart = false;

        if (cartOptional.isPresent()) {
            Cart existingCart = cartOptional.get();
            List<CartItem> existingItems = existingCart.getItems() != null ? existingCart.getItems() : new ArrayList<>();
            for (CartItem item : existingItems) {
                if (item.getSkuId() != null && item.getSkuId().equals(request.getSkuId())) {
                    itemExistsInCart = true;
                    break;
                }
            }
        }

        ProductResponseDto product;
        try {
            log.info("Fetching product details for SkuId: {}", request.getSkuId());
            ApiResponse<ProductResponseDto> productResponse = productFeign.getProduct(request.getSkuId());
            if (productResponse == null || productResponse.getData() == null) {
                throw new ResourceNotFoundException("Product not found with id: " + request.getSkuId());
            }
            product = productResponse.getData();
            log.info("Successfully fetched product details for SkuId: {}", request.getSkuId());
        } catch (FeignException.NotFound e) {
            log.warn("Product not found for SkuId: {}", request.getSkuId());

            if (itemExistsInCart) {
                try {
                    removeItemFromCart(userId, request.getSkuId());
                    log.info("Removed product with SkuId: {} from cart as it no longer exists in product service",
                            request.getSkuId());
                } catch (Exception ex) {
                    log.error("Error removing item from cart for SkuId: {}", request.getSkuId(), ex);
                }
                return Boolean.TRUE;
            } else {
                log.error("User attempted to add product with SkuId: {} which does not exist in product service",
                        request.getSkuId());
                throw new ResourceNotFoundException("Product with id " + request.getSkuId() + " does not exist. Cannot add to cart.");
            }
        } catch (FeignException e) {
            log.error("Error fetching product details for SkuId: {}, status: {}",
                    request.getSkuId(), e.status(), e);
            throw new BusinessException("Unable to fetch product details. Please try again later.");
        } catch (Exception e) {
            log.error("Unexpected error fetching product details for SkuId: {}",
                    request.getSkuId(), e);
            throw new BusinessException("An error occurred while fetching product details.");
        }

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
                .filter(item -> item.getSkuId().equals(request.getSkuId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(request.getQuantity());
            item.setPrice(product.getPrice());
            log.info("Updated quantity for existing SkuId: {} to {} (replaced, not added)",
                    request.getSkuId(), request.getQuantity());
        } else {
            CartItem newItem = new CartItem();
            newItem.setSkuId(request.getSkuId());
            newItem.setQuantity(request.getQuantity());
            newItem.setPrice(product.getPrice()); // Set price from product DB
            newItem.setAddedAt(new Date());
            items.add(newItem);
            log.info("Added new item to cart: SkuId: {}, quantity: {}",
                    request.getSkuId(), request.getQuantity());
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

        log.info("Successfully added/updated item in cart for userId: {}", userId);
        return Boolean.TRUE;
    }

    @Override
    public void removeItemFromCart(Long userId, String itemId) {
        log.info("Removing item from cart for userId: {}, itemId: {}", userId, itemId);

        if (userId == null || userId <= 0) {
            log.warn("Invalid userId provided: {}", userId);
            throw new ValidationException("Invalid userId. UserId must be a positive number.");
        }

        if (itemId == null || itemId.trim().isEmpty()) {  // Changed validation
            log.warn("Invalid itemId provided: {}", itemId);
            throw new ValidationException("Invalid itemId. ItemId cannot be null or empty.");
        }

        Optional<Cart> cartOptional = cartRepository.findByUserId(userId);
        if (!cartOptional.isPresent()) {
            log.warn("Cart not found for userId: {}", userId);
            throw new ResourceNotFoundException("Cart not found for userId: " + userId);
        }

        Cart cart = cartOptional.get();
        List<CartItem> items = cart.getItems() != null ? cart.getItems() : new ArrayList<>();

        boolean itemRemoved = items.removeIf(item -> item.getSkuId() != null && item.getSkuId().equals(itemId));  // Added null check

        if (!itemRemoved) {
            log.warn("Item with SkuId: {} not found in cart for userId: {}", itemId, userId);
            throw new ResourceNotFoundException("Item with SkuId " + itemId + " not found in cart.");
        }

        cart.setItems(items);
        cart.setUpdatedAt(new Date());

        try {
            cartRepository.save(cart);
            log.info("Successfully removed item with SkuId: {} from cart for userId: {}", itemId, userId);
        } catch (Exception e) {
            log.error("Error saving cart after item removal for userId: {}", userId, e);
            throw new BusinessException("Failed to remove item from cart. Please try again.");
        }
    }

    private CartItemDto mapToCartItemDto(CartItem cartItem, ProductResponseDto product) {
        CartItemDto dto = new CartItemDto();
        dto.setSkuId(cartItem.getSkuId());
        dto.setQuantity(cartItem.getQuantity());
        dto.setPrice(cartItem.getPrice());
        dto.setAddedAt(cartItem.getAddedAt());
        dto.setSkuId(product.getSkuId());
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