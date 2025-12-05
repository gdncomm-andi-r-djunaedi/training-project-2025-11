package com.example.api_gateway.exception;

public class InvalidCredentialsExceptionToken extends RuntimeException {

    public InvalidCredentialsExceptionToken(String message) {
        super(message);
    }
}
