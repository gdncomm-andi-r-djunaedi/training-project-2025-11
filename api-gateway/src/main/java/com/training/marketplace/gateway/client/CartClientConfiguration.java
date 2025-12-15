package com.training.marketplace.gateway.client;

import com.training.marketplace.cart.CartServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.ImportGrpcClients;

@Configuration
@ImportGrpcClients
public class CartClientConfiguration {

    @Bean
    public CartServiceGrpc.CartServiceBlockingStub cartSvcStub(){
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost",9093)
                .usePlaintext()
                .build();
        return CartServiceGrpc.newBlockingStub(channel);
    }
}
