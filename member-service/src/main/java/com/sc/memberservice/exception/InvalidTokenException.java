package com.sc.memberservice.exception;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String msg, Throwable cause) {
        super(msg, cause);
    }
}

