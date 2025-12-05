package com.apiGateWay.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
@Slf4j
@Component
public class CartValidationFilterFactory extends AbstractGatewayFilterFactory<CartValidationFilterFactory.Config> {

    private final JwtTokenService jwtTokenService;

    public CartValidationFilterFactory(JwtTokenService jwtTokenService) {
        super(Config.class);
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return new CartValidationFilter(jwtTokenService);
    }

    public static class Config {
    }

    @Override
    public String name() {
        return "CartValidation";
    }
}
