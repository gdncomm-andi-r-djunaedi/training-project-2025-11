package com.training.marketplace.cart.client;

import com.training.marketplace.product.controller.modal.request.GetProductDetailRequest;
import com.training.marketplace.product.controller.modal.request.GetProductDetailResponse;
import com.training.marketplace.product.service.ProductServiceGrpc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.grpc.client.ImportGrpcClients;

@ImportGrpcClients(target = "product")
public class ProductClientImpl {

    @Autowired
    ProductServiceGrpc.ProductServiceBlockingStub stub;

    public GetProductDetailResponse getProductDetail(String productId){
        return stub.getProductDetail(GetProductDetailRequest.newBuilder().setProductId(productId).build());
    }
}
