package com.blibi.cart.controller;

import com.blibi.cart.dto.AddToCartRequest;
import com.blibi.cart.dto.CartResponseDTO;
import com.blibi.cart.dto.GenericResponse;
import com.blibi.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for handling cart-related HTTP requests.
 * 
 * This controller provides endpoints for:
 * - Adding items to a user's cart
 * - Viewing cart contents
 * - Removing items from cart
 * 
 * All endpoints return responses wrapped in GenericResponse for consistent API
 * structure.
 * 
 * @RestController: Marks this class as a REST controller, automatically
 *                  serializes return objects to JSON
 * @Slf4j: Provides logging functionality via SLF4J
 * @RequiredArgsConstructor: Generates constructor for final fields (dependency
 *                           injection)
 * @RequestMapping: Base path for all endpoints in this controller
 * 
 * @author Cart Service Team
 * @version 1.0
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/cart")
public class CartController {

        /**
         * Service layer dependency for cart business logic.
         * Injected via constructor using @RequiredArgsConstructor.
         */
        private final CartService cartService;

        /**
         * Endpoint to add a product to the user's cart.
         * 
         * User ID is automatically extracted from JWT token by UserIdExtractionFilter
         * and passed as X-User-Id header. This improves security by preventing users
         * from accessing other users' carts.
         * 
         * If the product already exists in the cart, the quantity is updated.
         * The service fetches product details from Product Service to get current
         * pricing.
         * 
         * @param userIdStr User ID from JWT token (passed as header by API Gateway)
         * @param request   Request body containing productId and quantity to add
         * @return GenericResponse containing CartResponseDTO with updated cart details
         * 
         *         HTTP Method: POST
         *         Path: /api/cart/add
         * 
         *         Example Request:
         *         POST /api/cart/add
         *         Headers: Authorization: Bearer <jwt-token>
         *         Body: {"productId": "123", "quantity": 2}
         */
        @PostMapping("/add")
        public GenericResponse<CartResponseDTO> add(
                        @RequestHeader("X-User-Id") String userIdStr,
                        @RequestBody AddToCartRequest request) {
                UUID userId = UUID.fromString(userIdStr);
                // Build and return success response with cart data
                return GenericResponse.<CartResponseDTO>builder()
                                .status("SUCCESS")
                                .message("Item added to cart")
                                .data(cartService.addToCart(userId, request))
                                .build();
        }

        /**
         * Endpoint to retrieve the complete cart for a user.
         * 
         * User ID is automatically extracted from JWT token by UserIdExtractionFilter
         * and passed as X-User-Id header. This improves security by preventing users
         * from accessing other users' carts.
         * 
         * Returns all items in the cart along with the total cart price.
         * Throws CartNotFoundException if cart doesn't exist.
         * 
         * @param userIdStr User ID from JWT token (passed as header by API Gateway)
         * @return GenericResponse containing CartResponseDTO with cart details
         * 
         *         HTTP Method: GET
         *         Path: /api/cart
         * 
         *         Example Request:
         *         GET /api/cart
         *         Headers: Authorization: Bearer <jwt-token>
         */
        @GetMapping
        public GenericResponse<CartResponseDTO> view(
                        @RequestHeader("X-User-Id") String userIdStr) {
                UUID userId = UUID.fromString(userIdStr);
                // Build and return success response with cart data
                return GenericResponse.<CartResponseDTO>builder()
                                .status("SUCCESS")
                                .message("Cart fetched")
                                .data(cartService.getCart(userId))
                                .build();
        }

        /**
         * Endpoint to remove a specific product from the user's cart.
         * 
         * User ID is automatically extracted from JWT token by UserIdExtractionFilter
         * and passed as X-User-Id header. This improves security by preventing users
         * from accessing other users' carts.
         * 
         * Removes the product if it exists in the cart and recalculates the total
         * price.
         * Throws CartNotFoundException if cart doesn't exist.
         * 
         * @param userIdStr User ID from JWT token (passed as header by API Gateway)
         * @param productId Product identifier to remove from cart
         * @return GenericResponse containing CartResponseDTO with updated cart details
         * 
         *         HTTP Method: DELETE
         *         Path: /api/cart/remove/{productId}
         * 
         *         Example Request:
         *         DELETE /api/cart/remove/123
         *         Headers: Authorization: Bearer <jwt-token>
         */
        @DeleteMapping("/remove/{productId}")
        public GenericResponse<CartResponseDTO> remove(
                        @RequestHeader("X-User-Id") String userIdStr,
                        @PathVariable String productId) {
                UUID userId = UUID.fromString(userIdStr);
                // Build and return success response with updated cart data
                return GenericResponse.<CartResponseDTO>builder()
                                .status("SUCCESS")
                                .message("Item removed")
                                .data(cartService.removeItem(userId, productId))
                                .build();
        }
}
