package com.elfrida.member.exception;

public class EmailAlreadyRegisteredException extends RuntimeException{
    public EmailAlreadyRegisteredException(String email) {
        super("Email " + email + " already registered!");
    }
}
