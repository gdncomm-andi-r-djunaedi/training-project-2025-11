package com.kailash.apigateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {

    private T data;
    private boolean success;
    private String message;
    private Instant timestamp;

    public ApiResponse(T data, boolean success, String message) {
        this.data = data;
        this.success = success;
        this.message = message;
        this.timestamp = Instant.now();
    }

    // ✅ Correct success response
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data, true, "success");
    }

    public static <T> ApiResponse<T> failure(String message) {
        return new ApiResponse<>(null, false, message);
    }


    // ✅ Correct error response
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(null, false, message);
    }

}
