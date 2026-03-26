package com.ApiGateway.GateWay.exception;

import org.springframework.http.HttpStatus;

public class AuthException extends RuntimeException{
    public AuthException(String message, HttpStatus status) {
        super(message);
    }
}
