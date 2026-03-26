package com.Cart.CartService.controller;

import com.Cart.CartService.dto.CartResponseDTO;
import com.Cart.CartService.exception.CartServiceExceptions;
import com.Cart.CartService.token.JwtUtil;
import com.Cart.CartService.dto.AddItemRequestDTO;
import com.Cart.CartService.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    CartService cartService;

    @Autowired
    private JwtUtil jwtUtil;

    private String getMemberIdFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new CartServiceExceptions("Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }
        String token = authHeader.replace("Bearer ", "");
        if (!jwtUtil.isTokenValid(token)) {
            throw new CartServiceExceptions("Invalid or expired token", HttpStatus.UNAUTHORIZED);
        }
        return jwtUtil.extractUsername(token);
    }

    @PostMapping("/{memberId}/items")
    public ResponseEntity<CartResponseDTO> addItem(HttpServletRequest request, @PathVariable String memberId, @Valid @RequestBody AddItemRequestDTO requestDTO) {

        String userNameFromTokenToValidateToken = getMemberIdFromToken(request);
        if (userNameFromTokenToValidateToken == null || !userNameFromTokenToValidateToken.equals(memberId)) {
            throw new CartServiceExceptions("Token is invalid",HttpStatus.UNAUTHORIZED);
        }
        CartResponseDTO response = cartService.addItem(memberId, requestDTO);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<CartResponseDTO> getCart( HttpServletRequest request,@PathVariable String memberId) {
        String userNameFromTokenToValidateToken = getMemberIdFromToken(request);
        if (userNameFromTokenToValidateToken == null || !userNameFromTokenToValidateToken.equals(memberId)) {
            throw new CartServiceExceptions("Token is invalid",HttpStatus.UNAUTHORIZED);
        }
        CartResponseDTO response = cartService.getCartByMemberId(memberId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{username}/product/{itemId}")
    public ResponseEntity<Boolean> deleteProductFromCart(HttpServletRequest request, @PathVariable String username, @PathVariable String itemId) {
        String userNameFromTokenToValidateToken = getMemberIdFromToken(request);
        if (userNameFromTokenToValidateToken == null || !userNameFromTokenToValidateToken.equals(username)) {
            throw new CartServiceExceptions("Token is invalid",HttpStatus.UNAUTHORIZED);
        }
        boolean result = cartService.deleteProductFromCart(username, itemId);
        return ResponseEntity.ok(result);
    }

}
