package com.blibli.memberModule.exception;

public class ValidationException extends RuntimeException {
    private String errorCode;
    
    public ValidationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}

