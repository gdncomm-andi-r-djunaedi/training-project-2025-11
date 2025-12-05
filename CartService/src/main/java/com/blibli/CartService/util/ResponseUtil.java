package com.blibli.CartService.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseUtil {

    private ResponseUtil() {}

    public static <T> ResponseEntity<ApiResponse<T>> success(
            String message,
            T data
    ) {
        return ResponseEntity.ok(ApiResponse.success(message, data));
    }

    public static ResponseEntity<ApiResponse<Void>> success(String message) {
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(
            String message,
            T data
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(message, data));
    }

    public static ResponseEntity<ApiResponse<Void>> error(
            String message,
            HttpStatus status
    ) {
        return ResponseEntity
                .status(status)
                .body(ApiResponse.error(message));
    }
}