package org.jovian.member.exception;

public class InvalidLoginException extends RuntimeException {
    public InvalidLoginException() {
        super("Invalid username or password, please try again");
    }
}
