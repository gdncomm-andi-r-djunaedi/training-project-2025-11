package com.blibli.cartData.controller;

import com.blibli.cartData.dto.CartItemDTO;
import com.blibli.cartData.dto.CartProductDetailDTO;
import com.blibli.cartData.dto.CartResponseDTO;
import com.blibli.cartData.services.CartService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    CartService cartService;

    @PostMapping("/addItem")
    public ResponseEntity<CartResponseDTO> addItemToCart(@RequestHeader("Authorization") String authToken,
                                                         @RequestBody CartItemDTO cartItemDTO) {
        CartResponseDTO cartResponseDTO = cartService.addProductToCart(authToken, cartItemDTO);
        return new ResponseEntity<>(cartResponseDTO, HttpStatus.OK);
    }


    @DeleteMapping("/deleteItem/{productId}")
    public ResponseEntity<Boolean> deleteCartItemById(@RequestHeader("Authorization") String authToken, @PathVariable("productId") String productId) {
        cartService.deleteCartItem(authToken, productId);
        return new ResponseEntity<>(Boolean.TRUE, HttpStatus.OK);
    }


    @GetMapping("/viewItem")
    @Operation(summary = "View cart item details of member", description = "View all cart item details")
    public ResponseEntity<Page<CartProductDetailDTO>> getAllCartProducts(
            @RequestHeader("Authorization") String authToken,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CartProductDetailDTO> cartItems = cartService.getAllCartItems(authToken, pageable);
        return new ResponseEntity<>(cartItems, HttpStatus.OK);
    }
}
