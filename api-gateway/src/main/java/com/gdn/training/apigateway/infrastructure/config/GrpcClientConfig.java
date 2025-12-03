package com.gdn.training.apigateway.infrastructure.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.gdn.training.member.infrastructure.grpc.proto.MemberServiceGrpc;

@Configuration
public class GrpcClientConfig {

    @Bean(destroyMethod = "shutdown")
    public ManagedChannel memberServiceChannel(
            @Value("${grpc.member.host:localhost}") String host,
            @Value("${grpc.member.port:6566}") int port) {
        return ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext()
                .build();
    }

    @Bean
    public MemberServiceGrpc.MemberServiceBlockingStub memberServiceBlockingStub(
            ManagedChannel memberServiceChannel) {
        return MemberServiceGrpc.newBlockingStub(memberServiceChannel);
    }
}
