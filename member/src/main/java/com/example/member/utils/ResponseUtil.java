package com.example.member.utils;

import org.springframework.http.HttpStatus;

public class ResponseUtil {


    public static <T> APIResponse<T> success(int code, HttpStatus status, T data) {
        return new APIResponse<>(code, status, data, null, null);
    }

    public static <T> APIResponse<T> errorWithMessage(int code, HttpStatus status, String message) {
        return new APIResponse<>(code, status, null, null, message);
    }

    public static <T> APIResponse<T> error(int code, HttpStatus status) {
        return new APIResponse<>(code, status, null, null, null);
    }

    public static <T> APIResponse<T> successWithPagination(int code, HttpStatus status, T data, Object paging) {
        return new APIResponse<>(code, status, data, paging, null);
    }
}
