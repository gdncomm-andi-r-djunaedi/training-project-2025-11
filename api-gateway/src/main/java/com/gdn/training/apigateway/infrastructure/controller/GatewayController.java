package com.gdn.training.apigateway.infrastructure.controller;

import com.gdn.training.apigateway.application.dto.ProxyRequest;
import com.gdn.training.apigateway.application.usecase.ProxyToProductUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/gateway")
@Validated
public class GatewayController {

    private final ProxyToProductUseCase proxyToProductUseCase;

    public GatewayController(ProxyToProductUseCase proxyToProductUseCase) {
        this.proxyToProductUseCase = proxyToProductUseCase;
    }

    @PostMapping("/product/query")
    public ResponseEntity<String> queryProduct(
            @Valid @RequestBody ProxyRequest request,
            HttpServletRequest httpServletRequest) {

        Object claims = httpServletRequest.getAttribute("auth.claims");
        String response = proxyToProductUseCase.execute(request, claims);

        return ResponseEntity.ok(response);
    }
}
