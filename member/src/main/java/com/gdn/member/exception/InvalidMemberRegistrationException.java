package com.gdn.member.exception;

public class InvalidMemberRegistrationException extends RuntimeException {
    public InvalidMemberRegistrationException(String message) {
        super(message);
    }
}
