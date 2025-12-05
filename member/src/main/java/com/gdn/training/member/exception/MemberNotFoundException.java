package com.gdn.training.member.exception;

public class MemberNotFoundException extends RuntimeException {

    public MemberNotFoundException(String username) {
        super("Member not found: " + username);
    }
}
