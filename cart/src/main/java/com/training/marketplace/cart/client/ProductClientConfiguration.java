package com.training.marketplace.cart.client;

import com.training.marketplace.product.service.ProductServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;
import org.springframework.grpc.client.ImportGrpcClients;

@Configuration
@ImportGrpcClients
@NoArgsConstructor
public class ProductClientConfiguration {
    @Bean
    ProductServiceGrpc.ProductServiceBlockingStub productSvcStub(){
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost",9092)
                .usePlaintext()
                .build();
        return ProductServiceGrpc.newBlockingStub(channel);
    }
}
