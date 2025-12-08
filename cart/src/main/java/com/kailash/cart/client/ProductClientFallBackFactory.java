package com.kailash.cart.client;

import com.kailash.cart.dto.ApiResponse;
import com.kailash.cart.dto.ProductResponse;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;


@Component
public class ProductClientFallBackFactory implements FallbackFactory<ProductClient> {
    @Override
    public ProductClient create(Throwable cause) {
        return sku -> {
            ApiResponse<ProductResponse> response = new ApiResponse<>();
            response.setSuccess(false);
            response.setMessage("Product service unavailable. Please try again later.");
            response.setData(null);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        };
    }
}
