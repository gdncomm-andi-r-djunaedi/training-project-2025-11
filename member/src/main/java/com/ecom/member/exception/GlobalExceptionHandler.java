package com.ecom.member.exception;

import com.ecom.member.Dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

        @ExceptionHandler(WrongCredsException.class)
        public ApiResponse handleBadRequestException(WrongCredsException ex) {
            log.error("Bad request: {}", ex.getMessage());
            return ApiResponse.error(409, ex.getMessage());
        }

    }
