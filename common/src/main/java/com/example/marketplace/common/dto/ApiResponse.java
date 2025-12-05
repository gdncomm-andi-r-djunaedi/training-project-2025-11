package com.example.marketplace.common.dto;

public class ApiResponse<T> {
    private boolean success;
    private T data;
    private ErrorResponse error;

    public ApiResponse() {}
    public ApiResponse(boolean success, T data, ErrorResponse error) {
        this.success = success;
        this.data = data;
        this.error = error;
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> error(String message, int code) {
        return new ApiResponse<>(false, null, new ErrorResponse(code, message));
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    public ErrorResponse getError() { return error; }
    public void setError(ErrorResponse error) { this.error = error; }
}
