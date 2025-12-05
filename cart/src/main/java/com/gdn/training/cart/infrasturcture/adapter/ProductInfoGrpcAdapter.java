package com.gdn.training.cart.infrasturcture.adapter;

import com.gdn.training.cart.application.dto.ProductInfoResponse;
import com.gdn.training.cart.application.port.out.ProductInfoPort;
import com.gdn.training.product.grpc.GetProductByIdRequest;
import com.gdn.training.product.grpc.ProductResponse;
import com.gdn.training.product.grpc.ProductServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import io.grpc.Channel;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ProductInfoGrpcAdapter {
    private final ProductServiceGrpc.ProductServiceBlockingStub stub;

    public ProductInfoGrpcAdapter(@GrpcClient("product-service") Channel channel) {
        this.stub = ProductServiceGrpc.newBlockingStub(channel);
    }

    @Override
    public Map<UUID, ProductInfoResponse> fetchProductInfo(Collection<UUID> productIds) {

        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }

        Map<UUID, ProductInfoResponse> result = new HashMap<>();

        for (UUID productId : productIds) {

            GetProductByIdRequest req = GetProductByIdRequest.newBuilder()
                    .setProductId(productId.toString())
                    .build();

            ProductResponse pr = stub.getProductById(req);

            ProductInfoResponse dto = new ProductInfoResponse(
                    UUID.fromString(pr.getProductId()),
                    UUID.fromString(pr.getSellerId()),
                    pr.getName(),
                    pr.getDescription(),
                    pr.getCategory(),
                    pr.getPrice(),
                    pr.getStock(),
                    pr.getStatus(),
                    pr.getCreatedAt());

            result.put(productId, dto);
        }

        return result;
    }
}
