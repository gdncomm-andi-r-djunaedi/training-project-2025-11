package com.marketplace.member.exception;

public class InvalidCredentialsException extends MemberException {
    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}
