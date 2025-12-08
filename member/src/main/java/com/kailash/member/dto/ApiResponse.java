package com.kailash.member.dto;

import java.time.Instant;

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


    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
