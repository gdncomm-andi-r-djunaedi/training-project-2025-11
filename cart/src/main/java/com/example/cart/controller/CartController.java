package com.example.cart.controller;

import com.example.cart.dto.CartDTO;
import com.example.cart.dto.ProductDTO;
import com.example.cart.dto.response.GenericResponseSingleDTO;
import com.example.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart")
@Tag(name = "Cart", description = "Shopping cart management APIs")
public class CartController {
    private final CartService cartService;

    @Operation(summary = "Add product to cart")
    @ApiResponse(responseCode = "200", description = "Product added to cart successfully",
            content = @Content(schema = @Schema(implementation = GenericResponseSingleDTO.class)))
    @ApiResponse(responseCode = "201", description = "Cart created and product added",
            content = @Content(schema = @Schema(implementation = GenericResponseSingleDTO.class)))
    @PostMapping("/{cartId}/add")
    public GenericResponseSingleDTO<CartDTO> addProductToCart(
            @Parameter(description = "Cart ID (MongoDB ObjectId)", required = true, example = "507f1f77bcf86cd799439011")
            @PathVariable String cartId,
            @Valid @RequestBody ProductDTO productDTO) {

        log.debug("addProductToCart:: cartId - {}, productDTO - {}", cartId, productDTO);
        ObjectId objectId = new ObjectId(cartId);
        CartDTO cart = cartService.addProductToCart(objectId, productDTO);
        
        return new GenericResponseSingleDTO<>(
                HttpStatus.OK.value(),
                HttpStatus.OK.name(),
                cart
        );
    }

    @Operation(summary = "Get cart by ID")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved cart",
            content = @Content(schema = @Schema(implementation = GenericResponseSingleDTO.class)))
    @GetMapping("/{cartId}")
    public GenericResponseSingleDTO<CartDTO> getCart(
            @Parameter(description = "Cart ID (MongoDB ObjectId)", required = true, example = "507f1f77bcf86cd799439011")
            @PathVariable String cartId) {

        log.debug("getCart:: cartId - {}", cartId);
        ObjectId objectId = new ObjectId(cartId);
        CartDTO cart = cartService.getCart(objectId);
        return new GenericResponseSingleDTO<>(
                HttpStatus.OK.value(),
                HttpStatus.OK.name(),
                cart
        );
    }

    @Operation(summary = "Delete product from cart")
    @ApiResponse(responseCode = "200", description = "Product deleted from cart successfully",
            content = @Content(schema = @Schema(implementation = GenericResponseSingleDTO.class)))
    @DeleteMapping("/{cartId}/product/{productId}")
    public GenericResponseSingleDTO<CartDTO> deleteProductFromCart(
            @Parameter(description = "Cart ID (MongoDB ObjectId)", required = true, example = "507f1f77bcf86cd799439011")
            @PathVariable String cartId,
            @Parameter(description = "Product ID", required = true, example = "PROD001")
            @PathVariable String productId) {

        log.debug("deleteProductFromCart:: cartId - {}, productId - {}", cartId, productId);
        ObjectId objectId = new ObjectId(cartId);
        CartDTO cart = cartService.deleteProductFromCart(objectId, productId);
        return new GenericResponseSingleDTO<>(
                HttpStatus.OK.value(),
                HttpStatus.OK.name(),
                cart
        );
    }
}

