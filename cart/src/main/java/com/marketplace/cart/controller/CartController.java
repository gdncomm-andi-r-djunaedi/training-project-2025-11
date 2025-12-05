package com.marketplace.cart.controller;

import com.marketplace.cart.dto.*;
import com.marketplace.cart.service.CartService;
import com.marketplace.common.dto.ApiResponse;
import com.marketplace.common.exception.UnauthorizedException;
import com.marketplace.common.security.MemberContext;
import com.marketplace.common.security.MemberContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Shopping cart APIs - add, view, update, dan remove item dari cart")
@SecurityRequirement(name = "bearerAuth")
public class CartController {

    private final CartService cartService;

    @Operation(
            summary = "Get cart member",
            description = "Mengambil isi shopping cart untuk member yang sedang login. Data diambil dari Redis cache."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Berhasil mendapatkan cart",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Member belum login")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart() {
        UUID memberId = getCurrentMemberId();
        log.info("Get cart request for member: {}", memberId);
        CartResponse response = cartService.getCart(memberId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
            summary = "Add item ke cart",
            description = "Menambahkan produk ke cart. Jika produk sudah ada, quantity akan ditambahkan."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Item berhasil ditambahkan ke cart"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Member belum login"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validasi gagal (quantity harus > 0)")
    })
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            @Valid @RequestBody AddToCartRequest request) {
        UUID memberId = getCurrentMemberId();
        log.info("Add to cart request for member: {}, product: {}", memberId, request.getProductId());
        CartResponse response = cartService.addToCart(memberId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Item added to cart"));
    }

    @Operation(
            summary = "Update quantity item di cart",
            description = "Mengubah quantity item di cart. Jika quantity = 0, item akan dihapus."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Cart item berhasil diupdate"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Member belum login"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Item tidak ditemukan di cart")
    })
    @PutMapping("/items/{productId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateCartItem(
            @Parameter(description = "Product ID yang akan diupdate", required = true, example = "507f1f77bcf86cd799439011")
            @PathVariable String productId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        UUID memberId = getCurrentMemberId();
        log.info("Update cart item request for member: {}, product: {}", memberId, productId);
        CartResponse response = cartService.updateCartItem(memberId, productId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Cart item updated"));
    }

    @Operation(
            summary = "Remove item dari cart",
            description = "Menghapus produk dari cart berdasarkan product ID"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Item berhasil dihapus dari cart"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Member belum login")
    })
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeFromCart(
            @Parameter(description = "Product ID yang akan dihapus", required = true, example = "507f1f77bcf86cd799439011")
            @PathVariable String productId) {
        UUID memberId = getCurrentMemberId();
        log.info("Remove from cart request for member: {}, product: {}", memberId, productId);
        CartResponse response = cartService.removeFromCart(memberId, productId);
        return ResponseEntity.ok(ApiResponse.success(response, "Item removed from cart"));
    }

    @Operation(
            summary = "Clear cart",
            description = "Menghapus semua item dari cart member"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Cart berhasil dikosongkan"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Member belum login")
    })
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart() {
        UUID memberId = getCurrentMemberId();
        log.info("Clear cart request for member: {}", memberId);
        cartService.deleteCart(memberId);
        return ResponseEntity.ok(ApiResponse.success(null, "Cart cleared"));
    }

    private UUID getCurrentMemberId() {
        MemberContext context = MemberContextHolder.getContext();
        if (context == null || context.getMemberId() == null) {
            throw new UnauthorizedException("Not authenticated");
        }
        return context.getMemberId();
    }
}
