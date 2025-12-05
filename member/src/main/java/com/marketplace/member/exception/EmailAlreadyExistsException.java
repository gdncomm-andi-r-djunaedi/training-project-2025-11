package com.marketplace.member.exception;

public class EmailAlreadyExistsException extends MemberException {
    public EmailAlreadyExistsException(String email) {
        super("Email already exists: " + email);
    }
}
