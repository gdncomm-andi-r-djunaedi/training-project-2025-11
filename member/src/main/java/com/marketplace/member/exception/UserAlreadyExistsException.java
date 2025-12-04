package com.marketplace.member.exception;

import com.marketplace.common.exception.BaseException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a user already exists (duplicate email or phone
 * number).
 */
public class UserAlreadyExistsException extends BaseException {

    public UserAlreadyExistsException(String message) {
        super(message,
                HttpStatus.CONFLICT.value(),
                "USER_ALREADY_EXISTS");
    }

    public static UserAlreadyExistsException email(String email) {
        return new UserAlreadyExistsException("Email already exists: " + email);
    }

    public static UserAlreadyExistsException phoneNumber(String phoneNumber) {
        return new UserAlreadyExistsException("Phone number already exists: " + phoneNumber);
    }
}
