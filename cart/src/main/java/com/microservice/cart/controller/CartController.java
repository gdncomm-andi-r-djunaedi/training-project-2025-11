package com.microservice.cart.controller;

import com.microservice.cart.dto.AddToCartRequestDto;
import com.microservice.cart.dto.CartDto;
import com.microservice.cart.service.CartService;
import com.microservice.cart.wrapper.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartDto>> getCart(
            @RequestHeader("X-User-Id") Long userId) {
        log.info("GET /api/cart - Request received for userId: {}", userId);

        CartDto cartDto = cartService.getCart(userId);
        
        // Check if cart is empty and set appropriate message
        ApiResponse<CartDto> response;
        if (cartDto.getItems() == null || cartDto.getItems().isEmpty()) {
            if (cartDto.getTotalQuantity() == null || cartDto.getTotalQuantity() == 0) {
                response = ApiResponse.successWithMessage(cartDto, "Cart is empty", HttpStatus.OK);
                log.info("GET /api/cart - Cart is empty for userId: {}, returning empty cart with message", userId);
            } else {
                response = ApiResponse.success(cartDto, HttpStatus.OK);
            }
        } else {
            response = ApiResponse.success(cartDto, HttpStatus.OK);
        }

        log.info("GET /api/cart - Response sent successfully for userId: {}", userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/items")
    public ResponseEntity<?> addItemToCart(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody AddToCartRequestDto request) {
        log.info("POST /api/cart/items - Request received for userId: {}, skuId: {}, quantity: {}",
                userId,
                request != null ? request.getSkuId() : null,
                request != null ? request.getQuantity() : null);

        String result = cartService.addItemToCart(userId, request);
        
        // Determine status code and text based on result
        HttpStatus httpStatus;
        String statusText;
        CartDto emptyCart = null;
        
        if ("UPDATED".equals(result)) {
            httpStatus = HttpStatus.OK;
            statusText = "Updated";
            log.info("POST /api/cart/items - Item updated successfully for userId: {}, returning 200 OK", userId);
        } else if ("ADDED".equals(result)) {
            httpStatus = HttpStatus.CREATED;
            statusText = "Created";
            log.info("POST /api/cart/items - Item added successfully for userId: {}, returning 201 Created", userId);
        } else if ("EMPTY_CART".equals(result)) {
            // Cart became empty after removing deleted product
            httpStatus = HttpStatus.OK;
            statusText = "OK";
            emptyCart = new CartDto();
            emptyCart.setUserId(userId);
            emptyCart.setItems(new java.util.ArrayList<>());
            emptyCart.setUpdatedAt(new java.util.Date());
            emptyCart.setTotalQuantity(0);
            log.info("POST /api/cart/items - Cart is now empty for userId: {} after removing deleted product", userId);
        } else {
            // REMOVED case (item removed but cart still has other items)
            httpStatus = HttpStatus.OK;
            statusText = "Removed";
            log.info("POST /api/cart/items - Item removed successfully for userId: {}, returning 200 OK", userId);
        }
        
        if (emptyCart != null) {
            // Return empty cart with message
            ApiResponse<CartDto> cartResponse = ApiResponse.successWithMessage(emptyCart, "Cart is empty", httpStatus);
            cartResponse.setStatusText(statusText);
            return new ResponseEntity<>(cartResponse, httpStatus);
        } else {
            ApiResponse<Boolean> boolResponse = ApiResponse.success(true, httpStatus);
            boolResponse.setStatusText(statusText);
            return new ResponseEntity<>(boolResponse, httpStatus);
        }
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<Boolean>> removeItemFromCart(
            @PathVariable String itemId,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("DELETE /api/cart/items/{} - Request received for userId: {}", itemId, userId);

        cartService.removeItemFromCart(userId, itemId);
        ApiResponse<Boolean> response = ApiResponse.success(true, HttpStatus.NO_CONTENT);

        log.info("DELETE /api/cart/items/{} - Item removed successfully for userId: {}, returning 204 No Content",
                itemId, userId);
        return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
    }

}