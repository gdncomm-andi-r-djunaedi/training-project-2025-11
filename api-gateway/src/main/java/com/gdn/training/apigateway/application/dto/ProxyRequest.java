package com.gdn.training.apigateway.application.dto;

import jakarta.validation.constraints.NotBlank;

public record ProxyRequest(
        @NotBlank(message = "Product ID is required") String productId,
        String query) {

}
