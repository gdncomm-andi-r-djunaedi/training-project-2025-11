package com.example.api_gateway.config;

import com.example.api_gateway.exception.ErrorResponse;
import com.example.api_gateway.exception.ServiceException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FeignErrorDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        HttpStatus httpStatus = HttpStatus.valueOf(response.status());
        
        try {
            if (response.body() != null) {
                try {
                    byte[] bodyBytes = Util.toByteArray(response.body().asInputStream());
                    if (bodyBytes != null && bodyBytes.length > 0) {
                        String bodyString = new String(bodyBytes, StandardCharsets.UTF_8);
                        ErrorResponse errorResponse = objectMapper.readValue(bodyString, ErrorResponse.class);
                        return new ServiceException(errorResponse, httpStatus);
                    }
                } catch (IOException e) {
                    ErrorResponse errorResponse = new ErrorResponse(
                            "SERVICE_ERROR",
                            "Error from downstream service: " + httpStatus.getReasonPhrase(),
                            System.currentTimeMillis()
                    );
                    return new ServiceException(errorResponse, httpStatus);
                }
            }
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse(
                    "SERVICE_ERROR",
                    "Error from downstream service: " + httpStatus.getReasonPhrase(),
                    System.currentTimeMillis()
            );
            return new ServiceException(errorResponse, httpStatus);
        }
        ErrorResponse errorResponse = new ErrorResponse(
                "SERVICE_ERROR",
                "Error from downstream service: " + httpStatus.getReasonPhrase(),
                System.currentTimeMillis()
        );
        return new ServiceException(errorResponse, httpStatus);
    }
}

