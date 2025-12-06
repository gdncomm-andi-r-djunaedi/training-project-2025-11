package com.marketplace.member.exception;

import com.marketplace.common.exception.BaseException;
import com.marketplace.member.constant.MemberConstants;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a user is not found
 */
public class UserNotFoundException extends BaseException {

    public UserNotFoundException(String username) {
        super(String.format(MemberConstants.ErrorMessages.USER_NOT_FOUND, username),
                HttpStatus.NOT_FOUND.value(),
                "USER_NOT_FOUND");
    }
}
