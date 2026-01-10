package com.blibli.apigateway.client;

import com.blibli.apigateway.dto.request.CartDto;
import com.blibli.apigateway.dto.response.CartResponseDto;
import com.blibli.apigateway.dto.response.ViewCartResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
    name = "cart-service", 
    url = "${cart.service.url}"
)
public interface CartClient {

    @PostMapping("/api/cart/add")
    CartResponseDto addToCart(
            @RequestBody CartDto cartDto,
            @RequestHeader("Authorization") String token);

    @PostMapping("/api/cart/clear")
    String clearCart(@RequestHeader("Authorization") String token);

    @GetMapping("/api/cart/view")
    ViewCartResponseDto viewCart(@RequestHeader("Authorization") String token);

}