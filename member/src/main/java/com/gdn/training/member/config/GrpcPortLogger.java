package com.gdn.training.member.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.devh.boot.grpc.server.event.GrpcServerStartedEvent;

@Configuration
public class GrpcPortLogger {

    private static final Logger log = LoggerFactory.getLogger(GrpcPortLogger.class);

    @Bean
    public ApplicationListener<GrpcServerStartedEvent> grpcServerStartedListener() {
        return event -> {
            int port = event.getServer().getPort();
            log.info("gRPC Server started successfully on dynamic port: {}", port);
        };
    }
}
