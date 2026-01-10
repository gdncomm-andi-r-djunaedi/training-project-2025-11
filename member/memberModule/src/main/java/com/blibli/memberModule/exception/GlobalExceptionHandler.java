package com.blibli.memberModule.exception;

import com.blibli.memberModule.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.blibli.memberModule.controller")
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException e) {
        return new ResponseEntity<>(
            new ErrorResponse(e.getErrorCode(), e.getMessage()),
            HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExists(EmailAlreadyExistsException e) {
        return new ResponseEntity<>(
            new ErrorResponse("EMAIL_ALREADY_EXISTS", e.getMessage()),
            HttpStatus.CONFLICT
        );
    }
}

