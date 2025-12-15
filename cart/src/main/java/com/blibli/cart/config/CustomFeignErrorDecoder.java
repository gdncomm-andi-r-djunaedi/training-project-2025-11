package com.blibli.cart.config;

import com.blibli.cart.exception.BadRequestException;
import com.blibli.cart.exception.ExternalServiceException;
import com.blibli.cart.exception.ResourceNotFoundException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomFeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        log.error("Feign error: method={}, status={}, reason={}", 
                methodKey, response.status(), response.reason());

        switch (response.status()) {
            case 400:
                return new BadRequestException("Invalid request to product service");
            case 404:
                return new ResourceNotFoundException("Product not found");
            case 503:
            case 504:
                return new ExternalServiceException("Product service is temporarily unavailable");
            default:
                return defaultErrorDecoder.decode(methodKey, response);
        }
    }
}

