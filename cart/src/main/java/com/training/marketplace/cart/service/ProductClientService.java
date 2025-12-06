package com.training.marketplace.cart.service;

import com.training.marketplace.product.controller.modal.request.GetProductDetailRequest;
import com.training.marketplace.product.controller.modal.request.GetProductDetailResponse;
import com.training.marketplace.product.service.ProductServiceGrpc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.grpc.client.ImportGrpcClients;
import org.springframework.stereotype.Service;

@Service
@ImportGrpcClients(target = "product")
public class ProductClientService {

    @Autowired
    private ProductServiceGrpc.ProductServiceBlockingStub productSvcStub;

    public GetProductDetailResponse getProductDetail(String productId){
        return productSvcStub.getProductDetail(GetProductDetailRequest.newBuilder().setProductId(productId).build());
    }
}
