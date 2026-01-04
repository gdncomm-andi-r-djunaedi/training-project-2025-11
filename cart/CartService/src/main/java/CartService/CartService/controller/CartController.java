package CartService.CartService.controller;

import CartService.CartService.common.ApiResponse;
import CartService.CartService.common.ResponseUtil;
import CartService.CartService.dto.CartRequestDto;
import CartService.CartService.dto.CartResponseDto;
import CartService.CartService.service.CartService;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

        @Autowired
        CartService cartService;

        @PostMapping("/addToCart")
        public ApiResponse<CartResponseDto> addToCart(
                @RequestHeader("X-User-Id") String userId,
                @RequestBody CartRequestDto request
        ) {
            return ResponseUtil.success(cartService.addToCart(userId, request));
        }

        @GetMapping("/viewCart")
        public ApiResponse<CartResponseDto> viewCart(@RequestHeader("X-User-Id") String userId) {
            return ResponseUtil.success(cartService.viewCart(userId));
        }

        @DeleteMapping("/removeItem")
        public ApiResponse<CartResponseDto> removeItem(
                @RequestHeader("X-User-Id") String userId,
                @RequestParam String productId
        ) {
            return ResponseUtil.success(cartService.removeItem(userId, productId));
        }

        @DeleteMapping("/clearCart")
        public ApiResponse<CartResponseDto> clearCart(@RequestHeader("X-User-Id") String userId) {
            return ResponseUtil.success(cartService.clearCart(userId));
        }

}
