package com.blibli.memberModule.exception;

public class AuthenticationException extends RuntimeException  {
    public AuthenticationException(String message) {
        super(message);
    }
}
