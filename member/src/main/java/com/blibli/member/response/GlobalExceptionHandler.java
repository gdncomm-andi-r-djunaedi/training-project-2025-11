package com.blibli.member.response;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ErrorResponse.class)
    public ResponseEntity<GdnResponse> handleAppException(ErrorResponse ex) {
        log.error("Error on Member: "+ex);
        return new ResponseEntity<>(
                GdnResponse.error(ex.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GdnResponse> handleOtherException(Exception ex) {
        log.error("Error on Member: "+ex);
        return new ResponseEntity<>(
                GdnResponse.error(ex.getMessage()
                ), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
