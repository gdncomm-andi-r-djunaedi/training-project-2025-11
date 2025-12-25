package com.blibi.cart.serviceImpl;

import com.blibi.cart.dto.AddToCartRequest;
import com.blibi.cart.dto.CartItemDTO;
import com.blibi.cart.dto.CartResponseDTO;
import com.blibi.cart.dto.ProductFeignDTO;
import com.blibi.cart.entity.Cart;
import com.blibi.cart.entity.CartItem;
import com.blibi.cart.exception.CartNotFoundException;
import com.blibi.cart.feign.ProductFeignClient;
import com.blibi.cart.repository.CartRepository;
import com.blibi.cart.service.CartService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of CartService interface.
 * 
 * This class contains the business logic for cart operations including:
 * - Adding items to cart with product validation
 * - Retrieving cart details
 * - Removing items from cart
 * - Converting between entity and DTO objects
 * 
 * Key Features:
 * - Integrates with Product Service via Feign Client
 * - Handles cart creation if user doesn't have one
 * - Updates quantity for existing products
 * - Calculates total prices automatically
 * 
 * @Service: Marks this class as a Spring service component
 * @Slf4j: Provides logging functionality
 * @Data: Generates getters, setters, toString, equals, and hashCode
 * @RequiredArgsConstructor: Generates constructor for final fields
 * 
 * @author Cart Service Team
 * @version 1.0
 */
@Service
@Slf4j
@Data
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    /**
     * Repository for cart data access operations.
     * Provides CRUD operations for Cart entities in MongoDB.
     */
    private final CartRepository cartRepository;
    
    /**
     * Feign client for communicating with Product Service.
     * Used to fetch product details and pricing information.
     */
    private final ProductFeignClient productFeignClient;

    /**
     * Adds a product to the user's cart or updates quantity if product already exists.
     * 
     * Implementation Details:
     * 1. Retrieves existing cart or creates new one if user doesn't have a cart
     * 2. Fetches product details from Product Service using Feign Client
     * 3. Validates product exists and has valid price
     * 4. Checks if product already exists in cart
     * 5. Updates quantity if exists, otherwise adds new item
     * 6. Recalculates total cart price
     * 7. Saves cart to database
     * 8. Converts entity to DTO for response
     * 
     * @param userId Unique identifier for the user
     * @param request Contains productId and quantity to add
     * @return CartResponseDTO with updated cart information
     * @throws RuntimeException if product not found or price unavailable
     */
    @Override
    public CartResponseDTO addToCart(UUID userId, AddToCartRequest request) {
        log.info("Add to cart for user {} product {}", userId, request.getProductId());

        // Retrieve existing cart or create new cart if user doesn't have one
        // Uses Optional.orElse() to provide default cart if not found
        Cart cart = cartRepository.findById(userId)
                .orElse(Cart.builder()
                        .cartId(userId)
                        .items(new ArrayList<>())
                        .totalCartPrice(0.0)
                        .build());

        // Fetch product details from Product Service via Feign Client
        // This ensures we get the latest product information and pricing
        var productResponse = productFeignClient.getProduct(request.getProductId());
        var product = productResponse.getData();

        // Validate product exists and has valid price
        // Throws exception if product is null or price is unavailable
        if (product == null || product.getPrice() == null) {
            throw new RuntimeException("Product not found or price not available");
        }

        // Check if product already exists in cart using stream API
        // Uses filter to find matching productId and findFirst() to get Optional
        Optional<CartItem> existing = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(request.getProductId()))
                .findFirst();

        // Update existing item or add new item
        if (existing.isPresent()) {
            // Product already in cart: update quantity and recalculate item total
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            // Recalculate item total: quantity * unit price
            item.setItemTotalPrice(item.getQuantity() * product.getProductUnitPrice());
        } else {
            // Product not in cart: add new cart item
            cart.getItems().add(CartItem.builder()
                    .productId(request.getProductId())
                    .quantity(request.getQuantity())
                    // Calculate item total: quantity * unit price
                    .itemTotalPrice(request.getQuantity() * product.getProductUnitPrice())
                    .build());
        }
        
        // Recalculate total cart price by summing all item totals
        // Uses stream API with mapToDouble and sum() for efficient calculation
        cart.setTotalCartPrice(cart.getItems().stream()
                .mapToDouble(CartItem::getItemTotalPrice)
                .sum());
        
        // Persist cart changes to MongoDB
        cartRepository.save(cart);
        
        // Convert entity to DTO and return
        return convertToResponse(cart);
    }

    /**
     * Retrieves the complete cart for a user.
     * 
     * Implementation Details:
     * 1. Fetches cart from database using userId
     * 2. Throws exception if cart doesn't exist
     * 3. Converts entity to DTO for response
     * 
     * @param userId Unique identifier for the user
     * @return CartResponseDTO with cart details
     * @throws CartNotFoundException if cart doesn't exist
     */
    @Override
    public CartResponseDTO getCart(UUID userId) {
        log.info("Fetching cart for user {}", userId);
        
        // Retrieve cart from database, throw exception if not found
        // Uses Optional.orElseThrow() with lambda expression
        Cart cart = cartRepository.findById(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found"));
        
        // Convert entity to DTO and return
        return convertToResponse(cart);
    }

    /**
     * Removes a specific product from the user's cart.
     * 
     * Implementation Details:
     * 1. Retrieves cart from database
     * 2. Removes item matching productId using removeIf()
     * 3. Recalculates total cart price
     * 4. Saves updated cart to database
     * 5. Converts entity to DTO for response
     * 
     * @param userId Unique identifier for the user
     * @param productId Product identifier to remove
     * @return CartResponseDTO with updated cart information
     * @throws CartNotFoundException if cart doesn't exist
     */
    @Override
    public CartResponseDTO removeItem(UUID userId, String productId) {
        log.info("Removing product {} from cart {}", productId, userId);
        
        // Retrieve cart from database, throw exception if not found
        Cart cart = cartRepository.findById(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found"));

        // Remove item from cart using removeIf() with predicate
        // Removes all items matching the productId (should be only one)
        cart.getItems().removeIf(i -> i.getProductId().equals(productId));

        // Recalculate total cart price after item removal
        // Uses stream API to sum remaining item totals
        cart.setTotalCartPrice(cart.getItems().stream()
                .mapToDouble(CartItem::getItemTotalPrice)
                .sum());
        
        // Persist updated cart to database
        cartRepository.save(cart);
        
        // Convert entity to DTO and return
        return convertToResponse(cart);
    }

    /**
     * Private helper method to convert Cart entity to CartResponseDTO.
     * 
     * This method performs the conversion between the database entity (Cart)
     * and the data transfer object (CartResponseDTO) used for API responses.
     * 
     * Conversion Process:
     * 1. Creates list of CartItemDTO from CartItem entities
     * 2. Maps each CartItem to CartItemDTO with builder pattern
     * 3. Builds CartResponseDTO with total price and items list
     * 
     * This separation ensures:
     * - Entity structure is not exposed to API consumers
     * - DTO can evolve independently from entity
     * - Clean separation of concerns
     * 
     * @param cart Cart entity from database
     * @return CartResponseDTO for API response
     */
    private CartResponseDTO convertToResponse(Cart cart) {
        // Initialize list to hold converted cart items
        List<CartItemDTO> dtoList = new ArrayList<>();

        // Convert each CartItem entity to CartItemDTO
        // Iterates through cart items and builds DTOs using builder pattern
        for (CartItem item : cart.getItems()) {
            dtoList.add(CartItemDTO.builder()
                    .productId(item.getProductId())
                    .quantity(item.getQuantity())
                    .price(item.getItemTotalPrice())
                    .build());
        }

        // Build and return CartResponseDTO with total price and items
        return CartResponseDTO.builder()
                .totalCartPrice(cart.getTotalCartPrice())
                .items(dtoList)
                .build();
    }
}
