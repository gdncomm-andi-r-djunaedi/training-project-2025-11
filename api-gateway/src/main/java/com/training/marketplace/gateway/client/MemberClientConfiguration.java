package com.training.marketplace.gateway.client;

import com.training.marketplace.member.service.MemberServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;
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

