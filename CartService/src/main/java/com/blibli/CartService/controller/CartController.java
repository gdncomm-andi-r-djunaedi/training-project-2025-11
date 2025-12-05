package com.blibli.CartService.controller;

import com.blibli.CartService.dto.AddToCartRequest;
import com.blibli.CartService.dto.CartResponseDto;
import com.blibli.CartService.service.CartService;
import com.blibli.CartService.util.ApiResponse;
import com.blibli.CartService.util.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
//@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class CartController {

    private final CartService cartService;
//    private final JwtUtil jwtUtil;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/addToCart")
    public ResponseEntity<ApiResponse<CartResponseDto>> addToCart(@RequestHeader("X-User-Id") String userId ,
                                                                 @RequestBody AddToCartRequest addToCartRequest){

//        if (auth == null || !auth.startsWith("Bearer ")) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//        }

       // String userId = jwtUtil.extractUserId(auth.substring(7).trim());
        log.info("Received X-User-Id: {}", userId);
        CartResponseDto result = cartService.addOrUpdateCart(userId, addToCartRequest);
        return ResponseUtil.success("Product added to cart",result);

    }

    @GetMapping("/viewCart")
    public ResponseEntity<ApiResponse<CartResponseDto>> viewCart(@RequestHeader("X-User-Id") String userId){
//        String userId = jwtUtil.extractUserId(auth.substring(7));
        CartResponseDto result = cartService.viewCart(userId);
        return ResponseUtil.success("Product are displayed",result);
    }

    @DeleteMapping("/deleteItem")
    public ResponseEntity<ApiResponse<Void>> deleteItem(@RequestHeader("X-User-Id") String userId, @RequestParam String productId){

//        String userId = jwtUtil.extractUserId(auth.substring(7));
        log.info("Received X-User-Id: {}", userId);
        cartService.deleteItem(userId, productId);
        return ResponseUtil.success("Item is deleted from cart");
    }

}
