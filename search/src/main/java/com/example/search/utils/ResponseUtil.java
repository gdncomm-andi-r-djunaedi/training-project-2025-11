package com.example.search.utils;

public class ResponseUtil {

    public static <T> APIResponse<T> success(T data) {
        return new APIResponse<T>(true, data, null, null);
    }

    public static <T> APIResponse<T> error(String errorMessage, String errorCode) {
        return new APIResponse<T>(false, null, errorMessage, errorCode);
    }

}
