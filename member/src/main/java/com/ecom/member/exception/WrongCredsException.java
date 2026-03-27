package com.ecom.member.exception;

public class WrongCredsException extends RuntimeException {
    public WrongCredsException(String message) {
        super(message);
    }
}
