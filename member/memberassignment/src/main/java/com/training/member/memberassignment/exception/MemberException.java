package com.training.member.memberassignment.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class MemberException extends RuntimeException {
    private final HttpStatus status;
    private final String errorCode;

    public MemberException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public static MemberException emailAlreadyExists(String email) {
        return new MemberException("Email already registered", HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS");
    }

    public static MemberException invalidPassword() {
        return new MemberException("Password does not meet complexity rules", HttpStatus.BAD_REQUEST, "INVALID_PASSWORD");
    }

    public static MemberException invalidPayload() {
        return new MemberException("Invalid payload", HttpStatus.BAD_REQUEST, "INVALID_PAYLOAD");
    }

    public static MemberException invalidCredentials() {
        return new MemberException("Invalid email or password", HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
    }

    public static MemberException invalidToken() {
        return new MemberException("Invalid or expired token", HttpStatus.UNAUTHORIZED, "INVALID_TOKEN");
    }

    public static MemberException userNotFound() {
        return new MemberException("User not found", HttpStatus.NOT_FOUND, "USER_NOT_FOUND");
    }
}
