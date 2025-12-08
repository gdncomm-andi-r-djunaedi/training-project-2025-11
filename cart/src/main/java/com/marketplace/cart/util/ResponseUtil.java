package com.marketplace.cart.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseUtil {

    private ResponseUtil() {
        // Utility class - prevent instantiation
    }

    public static <T> ResponseEntity<ApiResponse<T>> success(T data) {
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> success(T data, String message) {
        return ResponseEntity.ok(ApiResponse.success(data, message));
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(T data) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(T data, String message) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(data, message));
    }

    public static <T> ResponseEntity<ApiResponse<T>> noContent() {
        return ResponseEntity.noContent().build();
    }

    public static <T> ResponseEntity<ApiResponse<T>> badRequest(String code, String description) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(code, description));
    }

    public static <T> ResponseEntity<ApiResponse<T>> badRequest(String code, String description, String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(code, description, message));
    }

    public static <T> ResponseEntity<ApiResponse<T>> unauthorized(String description) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("UNAUTHORIZED", description));
    }

    public static <T> ResponseEntity<ApiResponse<T>> forbidden(String description) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("FORBIDDEN", description));
    }

    public static <T> ResponseEntity<ApiResponse<T>> notFound(String code, String description) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(code, description));
    }

    public static <T> ResponseEntity<ApiResponse<T>> notFound(String code, String description, String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(code, description, message));
    }

    public static <T> ResponseEntity<ApiResponse<T>> conflict(String code, String description) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(code, description));
    }

    public static <T> ResponseEntity<ApiResponse<T>> internalServerError(String code, String description) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(code, description));
    }

    public static <T> ResponseEntity<ApiResponse<T>> error(HttpStatus status, String code, String description) {
        return ResponseEntity.status(status)
                .body(ApiResponse.error(code, description));
    }

    public static <T> ResponseEntity<ApiResponse<T>> error(HttpStatus status, String code, String description, String message) {
        return ResponseEntity.status(status)
                .body(ApiResponse.error(code, description, message));
    }
}

