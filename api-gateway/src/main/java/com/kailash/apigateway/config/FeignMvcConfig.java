package com.kailash.apigateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@Configuration
public class FeignMvcConfig {

    /**
     * Provide HttpMessageConverters bean so Feign (which expects MVC converters)
     * can encode/decode request bodies when used inside a reactive (WebFlux) app.
     */
    @Bean
    public HttpMessageConverters feignHttpMessageConverters(ObjectMapper objectMapper) {
        MappingJackson2HttpMessageConverter jacksonConverter =
                new MappingJackson2HttpMessageConverter(objectMapper);
        return new HttpMessageConverters(jacksonConverter);
    }
}
