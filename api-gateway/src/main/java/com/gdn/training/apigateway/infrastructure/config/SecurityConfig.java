package com.gdn.training.apigateway.infrastructure.config;

import com.gdn.training.apigateway.infrastructure.security.ApiGatewayFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final ApiGatewayFilter jwtFilter;

    @Bean
    public FilterRegistrationBean<ApiGatewayFilter> jwtFilterRegistration() {
        FilterRegistrationBean<ApiGatewayFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(jwtFilter);
        reg.addUrlPatterns("/*");
        reg.setOrder(1);
        return reg;
    }
}