package com.blibli.apigateway.exception;

public class TokenValidationException extends RuntimeException {
    private final String path;
    
    public TokenValidationException(String message, String path) {
        super(message);
        this.path = path;
    }
    
    public String getPath() {
        return path;
    }
}

