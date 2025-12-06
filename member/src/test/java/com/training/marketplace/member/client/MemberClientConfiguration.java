package com.training.marketplace.member.client;

import com.training.marketplace.member.service.MemberServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.ImportGrpcClients;

@Configuration
@ImportGrpcClients
public class MemberClientConfiguration {

    @Bean
    public MemberServiceGrpc.MemberServiceBlockingStub memberSvcStub(){
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost",9091)
                .usePlaintext()
                .build();
        return MemberServiceGrpc.newBlockingStub(channel);
    }
}

