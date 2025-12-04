package com.gdn.training.apigateway.application.usecase;

import com.gdn.training.apigateway.application.dto.ProxyRequest;
import com.gdn.training.apigateway.application.port.ProductGrpcPort;
import org.springframework.stereotype.Service;

@Service
public class ProxyToProductUseCase {
    private final ProductGrpcPort productGrpcPort;

    public ProxyToProductUseCase(ProductGrpcPort productGrpcPort) {
        this.productGrpcPort = productGrpcPort;
    }

    public String execute(ProxyRequest proxyRequest, Object claims) {
        return productGrpcPort.queryProduct(proxyRequest, claims);
    }
}
