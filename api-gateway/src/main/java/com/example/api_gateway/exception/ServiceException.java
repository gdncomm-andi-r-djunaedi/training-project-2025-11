package com.example.api_gateway.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ServiceException extends RuntimeException {
    private final ErrorResponse errorResponse;
    private final HttpStatus httpStatus;

    public ServiceException(ErrorResponse errorResponse, HttpStatus httpStatus) {
        super(errorResponse.getErrorMessage());
        this.errorResponse = errorResponse;
        this.httpStatus = httpStatus;
    }
}



