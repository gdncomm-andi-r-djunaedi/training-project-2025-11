package com.training.marketplace.member.filter;

import io.grpc.ServerInterceptor;
import io.grpc.ServerServiceDefinition;
import org.springframework.grpc.server.service.ServerInterceptorFilter;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthFilter implements ServerInterceptorFilter {

    @Override
    public boolean filter(ServerInterceptor interceptor, ServerServiceDefinition service) {
        return false;
    }
}
