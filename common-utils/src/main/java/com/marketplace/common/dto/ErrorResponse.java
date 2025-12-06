package com.marketplace.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Standardized error response for all services.
 * Used by GlobalExceptionHandler to provide consistent error messages.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /**
     * HTTP status code
     */
    private int status;

    /**
     * Error type/category
     */
    private String error;

    /**
     * Human-readable error message
     */
    private String message;

    /**
     * Detailed error messages (e.g., validation errors)
     */
    private List<String> details;

    /**
     * API path where the error occurred
     */
    private String path;

    /**
     * Timestamp when the error occurred
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Create a simple error response
     */
    public static ErrorResponse of(int status, String error, String message, String path) {
        return ErrorResponse.builder()
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .build();
    }

    /**
     * Create an error response with validation details
     */
    public static ErrorResponse withDetails(int status, String error, String message, List<String> details,
            String path) {
        return ErrorResponse.builder()
                .status(status)
                .error(error)
                .message(message)
                .details(details)
                .path(path)
                .build();
    }

    /**
     * Add a detail message to the error
     */
    public void addDetail(String detail) {
        if (this.details == null) {
            this.details = new ArrayList<>();
        }
        this.details.add(detail);
    }
}
