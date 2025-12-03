package com.gdn.training.apigateway.infrastructure.grpc;

import com.gdn.training.apigateway.application.dto.ProxyRequest;
import com.gdn.training.apigateway.application.port.ProductGrpcPort;
import io.grpc.ManagedChannel;
import org.springframework.stereotype.Component;

@Component
public class ProductGrpcClientImpl implements ProductGrpcPort {
    private final ManagedChannel channel;

    public ProductGrpcClientImpl(ManagedChannel channel) {
        this.channel = channel;
    }

    @Override
    public String queryProduct(ProxyRequest proxyRequest, Object claims) {
        return """
                {
                  "status": "ok",
                  "productId": "%s",
                  "q": "%s"
                }
                """.formatted(proxyRequest.productId(), proxyRequest.query() == null ? "" : proxyRequest.query());
    }
}