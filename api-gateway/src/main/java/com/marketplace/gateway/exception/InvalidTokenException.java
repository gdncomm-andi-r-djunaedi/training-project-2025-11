package com.marketplace.gateway.exception;

import com.marketplace.common.exception.BaseException;
import com.marketplace.gateway.constant.GatewayConstants;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when JWT token is invalid or expired
 */
public class InvalidTokenException extends BaseException {

    public InvalidTokenException(String message) {
        super(message,
                HttpStatus.UNAUTHORIZED.value(),
                "INVALID_TOKEN");
    }

    public static InvalidTokenException expired() {
        return new InvalidTokenException(GatewayConstants.ErrorMessages.EXPIRED_TOKEN);
    }

    public static InvalidTokenException malformed() {
        return new InvalidTokenException(GatewayConstants.ErrorMessages.MALFORMED_TOKEN);
    }

    public static InvalidTokenException missing() {
        return new InvalidTokenException(GatewayConstants.ErrorMessages.MISSING_TOKEN);
    }
}
