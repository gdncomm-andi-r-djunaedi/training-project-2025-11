package com.sc.utilsservice;

import lombok.Data;

import java.time.Instant;

@Data
public class ApiResponse<T> {
    private String timestamp = Instant.now().toString();
    private boolean success;
    private String errorCode;
    private String errorMessage;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setData(data);
        return response;
    }

    public static <T> ApiResponse<T> failure(String errorCode, String errorMessage) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setErrorCode(errorCode);
        response.setErrorMessage(errorMessage);
        return response;
    }
}
