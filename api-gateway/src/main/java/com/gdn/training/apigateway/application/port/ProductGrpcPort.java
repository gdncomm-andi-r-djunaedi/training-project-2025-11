package com.gdn.training.apigateway.application.port;

import com.gdn.training.apigateway.application.dto.ProxyRequest;

public interface ProductGrpcPort {
    String queryProduct(ProxyRequest proxyRequest, Object claims);
}
