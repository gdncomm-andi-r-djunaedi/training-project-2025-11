package com.blibli.api_gateway.Feign;

import com.blibli.api_gateway.dto.AddToCartRequestDTO;
import com.blibli.api_gateway.dto.AddToCartResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "cart",url = "http://localhost:8083/api")
public interface CartFeign {

    @PostMapping("/cart/addProductToCart")
    public ResponseEntity<AddToCartResponseDTO> addProductToCart(@RequestParam String customerEmail, @RequestBody AddToCartRequestDTO addToCartRequestDTO);

    @GetMapping("/cart/viewCart")
    public ResponseEntity<AddToCartResponseDTO> viewCart(@RequestParam String customerEmail);

    @DeleteMapping("/cart/deletBySku")
    public ResponseEntity<AddToCartResponseDTO> deletBySku(@RequestParam String customerEmail,@RequestParam String productSku);

    @DeleteMapping("/cart/deleteAllItems")
    public ResponseEntity<AddToCartResponseDTO> deleteAllItems(@RequestParam String customerEmail);

}
