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
            emptyCart.setTotalPrice(0L);
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
                List<ProductResponseDto> products;
                if (productsResponse != null && productsResponse.getData() != null) {
                    products = productsResponse.getData();
                } else {
                    products = new ArrayList<>();
                }
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
                log.warn("Error fetching product details in batch, status: {}. Returning empty cart.", e.status());
                CartDto emptyCart = new CartDto();
                emptyCart.setUserId(userId);
                emptyCart.setItems(new ArrayList<>());
                emptyCart.setUpdatedAt(new Date());
                emptyCart.setTotalQuantity(0);
                emptyCart.setTotalPrice(0L);
                return emptyCart;
            } catch (Exception e) {
                log.warn("Unexpected error fetching product details in batch. Returning empty cart.", e);
                CartDto emptyCart = new CartDto();
                emptyCart.setUserId(userId);
                emptyCart.setItems(new ArrayList<>());
                emptyCart.setUpdatedAt(new Date());
                emptyCart.setTotalQuantity(0);
                emptyCart.setTotalPrice(0L);
                return emptyCart;
            }
        }

        Integer totalQuantity = 0;
        Long totalPrice = 0L;

        for (CartItemDto cartItemDto : cartItemDtos) {
            Integer itemQuantity = cartItemDto.getQuantity();
            if (itemQuantity != null) {
                totalQuantity = totalQuantity + itemQuantity;
            }
            
            Long itemPrice = cartItemDto.getPrice();
            if (itemPrice != null && itemQuantity != null) {
                totalPrice = totalPrice + (itemPrice * itemQuantity);
            }
        }

        CartDto cartDto = new CartDto();
        cartDto.setUserId(cart.getUserId());
        cartDto.setItems(cartItemDtos);
        cartDto.setUpdatedAt(cart.getUpdatedAt());
        cartDto.setTotalQuantity(totalQuantity);
        cartDto.setTotalPrice(totalPrice);

        log.info("Successfully retrieved cart for userId: {} with {} items, total quantity: {}, total price: {}",
                userId, cartItemDtos.size(), totalQuantity, totalPrice);
        return cartDto;
    }

    @Override
    public String addItemToCart(Long userId, AddToCartRequestDto request) {
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

        if (request.getSkuId() == null || request.getSkuId().trim().isEmpty()) {
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
                    Optional<Cart> cartToCheck = cartRepository.findByUserId(userId);
                    if (cartToCheck.isPresent()) {
                        Cart cart = cartToCheck.get();
                        List<CartItem> items = cart.getItems() != null ? cart.getItems() : new ArrayList<>();
                        int itemCount = items.size();
                        
                        removeItemFromCart(userId, request.getSkuId());
                        log.info("Removed product with SkuId: {} from cart as it no longer exists in product service",
                                request.getSkuId());
                        
                        // If cart had only one item, it's now empty
                        if (itemCount == 1) {
                            log.info("Cart is now empty for userId: {} after removing deleted product", userId);
                            return "EMPTY_CART";
                        }
                    }
                } catch (Exception ex) {
                    log.error("Error removing item from cart for SkuId: {}", request.getSkuId(), ex);
                }
                return "REMOVED";
            } else {
                log.error("User attempted to add product with SkuId: {} which does not exist in product service",
                        request.getSkuId());
                throw new ResourceNotFoundException("Product with id " + request.getSkuId() + " does not exist. Cannot add to cart.");
            }
        } catch (FeignException e) {
            log.error("Error fetching product details for SkuId: {}, status: {}",
                    request.getSkuId(), e.status(), e);
            
            if (itemExistsInCart) {
                log.warn("Product service error for SkuId: {} that exists in cart. Returning empty cart.", request.getSkuId());
                try {
                    Optional<Cart> cartToCheck = cartRepository.findByUserId(userId);
                    if (cartToCheck.isPresent()) {
                        Cart cart = cartToCheck.get();
                        List<CartItem> items = cart.getItems() != null ? cart.getItems() : new ArrayList<>();
                        int itemCount = items.size();
                        
                        removeItemFromCart(userId, request.getSkuId());
                        
                        if (itemCount == 1) {
                            return "EMPTY_CART";
                        }
                        return "REMOVED";
                    }
                } catch (Exception ex) {
                    log.error("Error removing item from cart for SkuId: {}", request.getSkuId(), ex);
                }
                return "EMPTY_CART";
            }
            
            throw new BusinessException("Unable to fetch product details. Please try again later.");
        } catch (Exception e) {
            log.error("Unexpected error fetching product details for SkuId: {}",
                    request.getSkuId(), e);
            
            if (itemExistsInCart) {
                log.warn("Unexpected error for SkuId: {} that exists in cart. Returning empty cart.", request.getSkuId());
                try {
                    Optional<Cart> cartToCheck = cartRepository.findByUserId(userId);
                    if (cartToCheck.isPresent()) {
                        Cart cart = cartToCheck.get();
                        List<CartItem> items = cart.getItems() != null ? cart.getItems() : new ArrayList<>();
                        int itemCount = items.size();
                        
                        removeItemFromCart(userId, request.getSkuId());
                        
                        if (itemCount == 1) {
                            return "EMPTY_CART";
                        }
                        return "REMOVED";
                    }
                } catch (Exception ex) {
                    log.error("Error removing item from cart for SkuId: {}", request.getSkuId(), ex);
                }
                return "EMPTY_CART";
            }
            
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
        }

        List<CartItem> items = cart.getItems() != null ? cart.getItems() : new ArrayList<>();
        Optional<CartItem> existingItem = items.stream()
                .filter(item -> item.getSkuId().equals(request.getSkuId()))
                .findFirst();

        String result;
        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            
            Long oldItemTotal = item.getPrice() != null && item.getQuantity() != null
                ? item.getPrice() * item.getQuantity() : 0L;
            Long newItemTotal = product.getPrice() != null && request.getQuantity() != null 
                ? product.getPrice() * request.getQuantity() : 0L;
            
            item.setQuantity(request.getQuantity());
            item.setPrice(product.getPrice());
            result = "UPDATED";
            log.info("Updated quantity for existing SkuId: {} to {} (replaced, not added)",
                    request.getSkuId(), request.getQuantity());
        } else {
            CartItem newItem = new CartItem();
            newItem.setSkuId(request.getSkuId());
            newItem.setQuantity(request.getQuantity());
            newItem.setPrice(product.getPrice());
            newItem.setAddedAt(new Date());
            items.add(newItem);
            result = "ADDED";
            log.info("Added new item to cart: SkuId: {}, quantity: {}",
                    request.getSkuId(), request.getQuantity());
        }

        cart.setItems(items);
        cart.setUpdatedAt(new Date());
        
        Long totalPrice = 0L;
        Integer totalQuantity = 0;
        for (CartItem cartItem : items) {
            if (cartItem.getPrice() != null && cartItem.getQuantity() != null) {
                totalPrice = totalPrice + (cartItem.getPrice() * cartItem.getQuantity());
                totalQuantity = totalQuantity + cartItem.getQuantity();
            }
        }
        cart.setTotalPrice(totalPrice);
        cart.setTotalQuantity(totalQuantity);

        try {
            cart = cartRepository.save(cart);
            log.info("Cart saved successfully for userId: {} with total price: {}, total quantity: {}", 
                    userId, totalPrice, totalQuantity);
        } catch (Exception e) {
            log.error("Error saving cart for userId: {}", userId, e);
            throw new BusinessException("Failed to save cart. Please try again.");
        }

        log.info("Successfully {} item in cart for userId: {}", result.toLowerCase(), userId);
        return result;
    }

    @Override
    public void removeItemFromCart(Long userId, String itemId) {
        log.info("Removing item from cart for userId: {}, itemId: {}", userId, itemId);

        if (userId == null || userId <= 0) {
            log.warn("Invalid userId provided: {}", userId);
            throw new ValidationException("Invalid userId. UserId must be a positive number.");
        }

        if (itemId == null || itemId.trim().isEmpty()) {
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

        Long removedItemPrice = 0L;
        Integer removedItemQuantity = 0;
        boolean itemRemoved = false;
        
        for (CartItem item : items) {
            if (item.getSkuId() != null && item.getSkuId().equals(itemId)) {
                // Calculate the price of the item being removed
                if (item.getPrice() != null && item.getQuantity() != null) {
                    removedItemPrice = item.getPrice() * item.getQuantity();
                    removedItemQuantity = item.getQuantity();
                }
                items.remove(item);
                itemRemoved = true;
                break;
            }
        }

        if (!itemRemoved) {
            log.warn("Item with SkuId: {} not found in cart for userId: {}", itemId, userId);
            throw new ResourceNotFoundException("Item with SkuId " + itemId + " not found in cart.");
        }

        cart.setItems(items);
        cart.setUpdatedAt(new Date());
        Long totalPrice = 0L;
        Integer totalQuantity = 0;
        for (CartItem cartItem : items) {
            if (cartItem.getPrice() != null && cartItem.getQuantity() != null) {
                totalPrice = totalPrice + (cartItem.getPrice() * cartItem.getQuantity());
                totalQuantity = totalQuantity + cartItem.getQuantity();
            }
        }
        cart.setTotalPrice(totalPrice);
        cart.setTotalQuantity(totalQuantity);

        try {
            cartRepository.save(cart);
            log.info("Successfully removed item with SkuId: {} from cart for userId: {}. Removed price: {}, new total price: {}, new total quantity: {}", 
                    itemId, userId, removedItemPrice, totalPrice, totalQuantity);
        } catch (Exception e) {
            log.error("Error saving cart after item removal for userId: {}", userId, e);
            throw new BusinessException("Failed to remove item from cart. Please try again.");
        }
    }

    @Override
    public String updateItemQuantity(Long userId, String itemSku, Integer quantity, String operation) {
        log.info("Updating item quantity for userId: {}, itemSku: {}, quantity: {}, operation: {}",
                userId, itemSku, quantity, operation);

        if (userId == null || userId <= 0) {
            log.warn("Invalid userId provided: {}", userId);
            throw new ValidationException("Invalid userId. UserId must be a positive number.");
        }

        if (itemSku == null || itemSku.trim().isEmpty()) {
            log.warn("Invalid itemSku provided: {}", itemSku);
            throw new ValidationException("Invalid itemSku. ItemSku cannot be null or empty.");
        }

        if (quantity == null || quantity <= 0) {
            log.warn("Invalid quantity provided: {}", quantity);
            throw new ValidationException("Invalid quantity. Quantity must be a positive number.");
        }

        if (operation == null || operation.trim().isEmpty()) {
            log.warn("Invalid operation provided: {}", operation);
            throw new ValidationException("Invalid operation. Operation must be 'increase' or 'decrease'.");
        }

        String normalizedOperation = operation.trim().toLowerCase();
        if (!"increase".equals(normalizedOperation) && !"decrease".equals(normalizedOperation)) {
            log.warn("Invalid operation value: {}. Must be 'increase' or 'decrease'", operation);
            throw new ValidationException("Invalid operation. Operation must be 'increase' or 'decrease'.");
        }

        Optional<Cart> cartOptional = cartRepository.findByUserId(userId);
        if (!cartOptional.isPresent()) {
            log.warn("Cart not found for userId: {}", userId);
            throw new ResourceNotFoundException("Cart not found for userId: " + userId);
        }

        Cart cart = cartOptional.get();
        List<CartItem> items = cart.getItems() != null ? cart.getItems() : new ArrayList<>();

        CartItem itemToUpdate = null;
        for (CartItem item : items) {
            if (item.getSkuId() != null && item.getSkuId().equals(itemSku)) {
                itemToUpdate = item;
                break;
            }
        }

        if (itemToUpdate == null) {
            log.warn("Item with SkuId: {} not found in cart for userId: {}", itemSku, userId);
            throw new ResourceNotFoundException("Item with SkuId " + itemSku + " not found in cart.");
        }

        Integer currentQuantity = itemToUpdate.getQuantity() != null ? itemToUpdate.getQuantity() : 0;
        Integer newQuantity;

        if ("increase".equals(normalizedOperation)) {
            newQuantity = currentQuantity + quantity;
            log.info("Increasing quantity for SkuId: {} from {} to {}", itemSku, currentQuantity, newQuantity);
        } else {
            newQuantity = currentQuantity - quantity;
            if (newQuantity <= 0) {
                log.warn("Decreasing quantity for SkuId: {} would result in {} or less. Removing item from cart.", 
                        itemSku, newQuantity);
                items.remove(itemToUpdate);
                cart.setItems(items);
                cart.setUpdatedAt(new Date());
                
                try {
                    cartRepository.save(cart);
                    log.info("Item with SkuId: {} removed from cart as quantity became 0 or negative", itemSku);
                    return "REMOVED";
                } catch (Exception e) {
                    log.error("Error saving cart after item removal for userId: {}", userId, e);
                    throw new BusinessException("Failed to update cart. Please try again.");
                }
            }
            log.info("Decreasing quantity for SkuId: {} from {} to {}", itemSku, currentQuantity, newQuantity);
        }

        itemToUpdate.setQuantity(newQuantity);
        cart.setItems(items);
        cart.setUpdatedAt(new Date());

        try {
            cartRepository.save(cart);
            log.info("Successfully updated quantity for SkuId: {} to {} for userId: {}", 
                    itemSku, newQuantity, userId);
            return "UPDATED";
        } catch (Exception e) {
            log.error("Error saving cart after quantity update for userId: {}", userId, e);
            throw new BusinessException("Failed to update cart. Please try again.");
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