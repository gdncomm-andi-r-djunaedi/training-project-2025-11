package com.MarketPlace.MemberService.exceptions;

import org.springframework.http.HttpStatus;

public class MemberServiceException extends RuntimeException {
    public MemberServiceException(String message, HttpStatus status) {
        super(message);
    }
}
