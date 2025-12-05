package com.microservice.cart.wrapper;

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

    private boolean success;
    private String errorMessage;
    private String errorCode;
    private String message;  // For informational messages in success cases

    private int status;
    private String statusText;

    private Instant timestamp;

    private T data;


    // Success with explicit HttpStatus
    public static <T> ApiResponse<T> success(T data, HttpStatus httpStatus) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setErrorMessage(null);
        response.setErrorCode(null);
        response.setMessage(null);
        response.setStatus(httpStatus.value());
        response.setStatusText(httpStatus.getReasonPhrase());
        response.setTimestamp(Instant.now());
        response.setData(data);
        return response;
    }
    
    // Success with message
    public static <T> ApiResponse<T> successWithMessage(T data, String message, HttpStatus httpStatus) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setErrorMessage(null);
        response.setErrorCode(null);
        response.setMessage(message);
        response.setStatus(httpStatus.value());
        response.setStatusText(httpStatus.getReasonPhrase());
        response.setTimestamp(Instant.now());
        response.setData(data);
        return response;
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(data, HttpStatus.OK);
    }

    public static <T> ApiResponse<T> error(String errorMessage,
                                           String errorCode,
                                           HttpStatus httpStatus) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setErrorMessage(errorMessage);
        response.setErrorCode(errorCode);
        response.setMessage(null);
        response.setStatus(httpStatus.value());
        response.setStatusText(httpStatus.getReasonPhrase());
        response.setTimestamp(Instant.now());
        response.setData(null);
        return response;
    }

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