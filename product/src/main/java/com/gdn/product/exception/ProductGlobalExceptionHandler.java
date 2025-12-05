package com.gdn.product.exception;

import com.gdn.product.dto.response.ApiResponse;
import com.gdn.product.dto.response.ErrorDetailsDTO;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class ProductGlobalExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiResponse<ErrorDetailsDTO>> handleProductNotFound(ProductNotFoundException ex) {
        log.warn("ProductNotFoundException: {}", ex.getMessage());

        ErrorDetailsDTO error = new ErrorDetailsDTO(
                "PRODUCT_NOT_FOUND",
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(ex.getMessage(), error));
    }

    @ExceptionHandler(InvalidSearchRequestException.class)
    public ResponseEntity<ApiResponse<ErrorDetailsDTO>> handleInvalidSearch(InvalidSearchRequestException ex) {
        log.warn("InvalidSearchRequestException: {}", ex.getMessage());

        ErrorDetailsDTO error = new ErrorDetailsDTO(
                "INVALID_SEARCH_REQUEST",
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(ex.getMessage(), error));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ErrorDetailsDTO>> handleGeneric(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);

        ErrorDetailsDTO error = new ErrorDetailsDTO(
                "INTERNAL_ERROR",
                "Unexpected error occurred"
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail("Something went wrong", error));
    }
}
