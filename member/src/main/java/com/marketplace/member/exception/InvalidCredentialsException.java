package com.marketplace.member.exception;

import com.marketplace.common.exception.BaseException;
import com.marketplace.member.constant.MemberConstants;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when login credentials are invalid
 */
public class InvalidCredentialsException extends BaseException {

    public InvalidCredentialsException() {
        super(MemberConstants.ErrorMessages.INVALID_CREDENTIALS,
                HttpStatus.UNAUTHORIZED.value(),
                "INVALID_CREDENTIALS");
    }
}
