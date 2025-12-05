package com.kailash.cart.dto;

import java.time.Instant;

public class ApiResponse<T> {

    private Instant timestamp;
    private boolean success;
    private String message;
    private T data;

    public ApiResponse() {
        this.timestamp = Instant.now();
    }

    public ApiResponse(boolean success, String message, T data) {
        this.timestamp = Instant.now();
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(T data) {
        this.data = data;
    }
}
