package org.jovian.member.exception;

public class MemberAlreadyExistsException extends RuntimeException{
    public MemberAlreadyExistsException(String message) {
        super(message);
    }
}
