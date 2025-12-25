package com.blibi.member.exception;

public class MemberNotFoundException extends RuntimeException{
    public MemberNotFoundException(String memberNotFound) {
        super(memberNotFound);}
}
