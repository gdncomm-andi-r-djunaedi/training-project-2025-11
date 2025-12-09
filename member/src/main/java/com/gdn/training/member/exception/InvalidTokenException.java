package com.gdn.training.member.exception;

public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException() {
        super("Invalid or missing token");
    }
}
