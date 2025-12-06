package com.training.marketplace.gateway.client;

import com.training.marketplace.product.controller.modal.request.GetProductDetailRequest;
import com.training.marketplace.product.controller.modal.request.GetProductDetailResponse;
import com.training.marketplace.product.controller.modal.request.GetProductListRequest;
import com.training.marketplace.product.controller.modal.request.GetProductListResponse;
import com.training.marketplace.product.service.ProductServiceGrpc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.grpc.client.ImportGrpcClients;

@ImportGrpcClients(target = "product", prefix = "product")
public class ProductClientImpl {

    @Autowired
    private ProductServiceGrpc.ProductServiceBlockingStub productSvcStub;

    public GetProductDetailResponse getProductDetail(GetProductDetailRequest request){
        return productSvcStub.getProductDetail(request);
    }

    public GetProductListResponse getProductList(GetProductListRequest request){
        return productSvcStub.getProductList(request);
    }
}
