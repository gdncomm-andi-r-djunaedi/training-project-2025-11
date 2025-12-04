package com.gdn.training.apigateway.infrastructure.security;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class AuthenticationFilterChain {
    private final List<GatewayFilter> filters;

    public AuthenticationFilterChain(List<GatewayFilter> filters) {
        this.filters = filters;
    }

    public boolean execute(HttpServletRequest request, HttpServletResponse response) {
        for (GatewayFilter filter : filters) {
            try {
                if (!filter.handle(request, response)) {
                    return false;
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                log.error("Error in filter", e);
            }
        }
        return true;
    }
}
