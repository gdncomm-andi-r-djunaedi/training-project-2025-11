package com.training.marketplace.cart.client;

import com.training.marketplace.product.controller.modal.request.GetProductDetailRequest;
import com.training.marketplace.product.controller.modal.request.GetProductDetailResponse;
import com.training.marketplace.product.service.ProductServiceGrpc;
import io.grpc.ManagedChannel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.grpc.client.ImportGrpcClients;
import org.springframework.stereotype.Service;

@Service
@ImportGrpcClients(target = "product")
@NoArgsConstructor
@AllArgsConstructor
public class ProductClientTestService {

    private ProductServiceGrpc.ProductServiceBlockingStub productSvcStub;
    private ManagedChannel managedChannel;

    public ProductClientTestService(ManagedChannel channel){
        this.managedChannel = channel;
        this.productSvcStub = ProductServiceGrpc.newBlockingStub(channel);
    }

    public GetProductDetailResponse getProductDetail(String productId){
        return productSvcStub.getProductDetail(GetProductDetailRequest.newBuilder().setProductId(productId).build());
    }
}
