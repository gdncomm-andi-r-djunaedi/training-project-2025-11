package com.training.cartService.cartmongo.controller;

import com.training.cartService.cartmongo.dto.AddToCartRequest;
import com.training.cartService.cartmongo.dto.CartResponse;
import com.training.cartService.cartmongo.model.ApiResponse;
import com.training.cartService.cartmongo.service.CartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.*;

@RestController
@RequestMapping("/cart")
@Api(tags = "Cart Management")
public class CartController
{

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/addToCart")
    @Operation(summary = "Add product to cart", description = "Adds a product to the user's cart with specified quantity")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            @ApiParam(value = "User ID (set by API Gateway)", required = true) @RequestHeader("X-User-Id") String userId,
            @ApiParam(value = "Add to cart request containing productId and quantity", required = true) @Valid @RequestBody AddToCartRequest request)
    {
        CartResponse cartResponse = cartService.addToCart(userId, request);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(cartResponse, "Product added to cart successfully"));
    }

    @GetMapping("/items")
    @Operation(summary = "Get cart items", description = "Retrieves all items in the user's cart")
    public ResponseEntity<ApiResponse<CartResponse>> getCartItems(
            @ApiParam(value = "User ID (set by API Gateway)", required = true) @RequestHeader("X-User-Id") String userId)
    {
        CartResponse cartResponse = cartService.getCart(userId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(cartResponse, "Cart retrieved successfully"));
    }

    @DeleteMapping("/deleteCartItems")
    @Operation(summary = "Delete all cart items", description = "Deletes all items from the user's cart")
    public ResponseEntity<ApiResponse<CartResponse>> deleteCartItems(
            @ApiParam(value = "User ID (set by API Gateway)", required = true) @RequestHeader("X-User-Id") String userId)
    {
        CartResponse cartResponse = cartService.deleteCartItems(userId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(cartResponse,"Cart items deleted successfully"));
    }


}
