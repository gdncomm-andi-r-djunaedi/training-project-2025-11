package com.blibli.cart.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class FilterConfig {

    @Value("${gateway.secret:GatewaySecretKeyForServiceVerification2024}")
    private String gatewaySecret;

    @Bean
    public FilterRegistrationBean<GatewaySecurityFilter> gatewaySecurityFilterRegistration() {
        FilterRegistrationBean<GatewaySecurityFilter> registration = new FilterRegistrationBean<>();
        GatewaySecurityFilter filter = new GatewaySecurityFilter();
        filter.setGatewaySecret(gatewaySecret);
        registration.setFilter(filter);
        registration.addUrlPatterns("/api/cart", "/api/cart/*"); // Apply to all cart endpoints
        registration.setOrder(1); // Execute first
        registration.setName("gatewaySecurityFilter");
        registration.setEnabled(true);
        return registration;
    }
}

