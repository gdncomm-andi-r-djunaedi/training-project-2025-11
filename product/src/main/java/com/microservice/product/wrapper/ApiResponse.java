package com.microservice.product.wrapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

import org.springframework.http.HttpStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    // Core fields similar to GDN
    private boolean success;
    private String errorMessage;
    private String errorCode;

    // HTTP status info
    private int status;          // HTTP status code: 200, 400, 404, ...
    private String statusText;   // "OK", "BAD_REQUEST", "NOT_FOUND", ...

    // Remove @JsonFormat - let Jackson serialize Instant as ISO-8601 string
    private Instant timestamp;   // when this response was created

    // Actual payload
    private T data;

    // ---------- Static factory methods ----------

    // Success with explicit HttpStatus
    public static <T> ApiResponse<T> success(T data, HttpStatus httpStatus) {
        return new ApiResponse<>(
                true,
                null,
                null,
                httpStatus.value(),
                httpStatus.getReasonPhrase(),
                Instant.now(),
                data
        );
    }

    // Success with default 200 OK (if you don't want to pass status always)
    public static <T> ApiResponse<T> success(T data) {
        return success(data, HttpStatus.OK);
    }

    // Error with HttpStatus + errorCode + message
    public static <T> ApiResponse<T> error(String errorMessage,
                                           String errorCode,
                                           HttpStatus httpStatus) {
        return new ApiResponse<>(
                false,
                errorMessage,
                errorCode,
                httpStatus.value(),
                httpStatus.getReasonPhrase(),
                Instant.now(),
                null
        );
    }

    // Error with default 400 BAD_REQUEST
    public static <T> ApiResponse<T> error(String errorMessage,
                                           String errorCode) {
        return error(errorMessage, errorCode, HttpStatus.BAD_REQUEST);
    }

    @Override
    public String toString() {
        return String.format(
                "ApiResponse [success=%s, status=%d, statusText=%s, errorCode=%s, errorMessage=%s, data=%s]",
                success, status, statusText, errorCode, errorMessage, data
        );
    }
}