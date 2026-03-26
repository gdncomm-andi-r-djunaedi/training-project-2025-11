package com.blibli.apiGateway.client;

import com.blibli.apiGateway.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "cartService", url = "${cart.service.url}")
public interface CartClient {

    @PostMapping("/api/cart/addItem")
    ResponseEntity<CartResponseDTO> addProductToCart(@RequestHeader(value = "Authorization") String authHeader,
                                                     @RequestBody CartItemDTO cartItemDTO);


    @DeleteMapping("/api/cart/deleteItem/{productId}")
    ResponseEntity<?> deleteProductFromCart(@RequestHeader("Authorization") String authToken,
                                            @PathVariable("productId") String productId);


    @GetMapping("/api/cart/viewItem")
    ResponseEntity<Page<CartProductDetailDTO>> viewCartItems(@RequestHeader("Authorization") String authToken,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "5") int size);

}
