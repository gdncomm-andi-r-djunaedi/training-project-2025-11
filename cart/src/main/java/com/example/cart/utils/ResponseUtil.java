package com.example.cart.utils;

public class ResponseUtil {

    public static <T> APIResponse<T> success(T data) {
        return new APIResponse<>(true, data, null, null);
    }

    public static <T> APIResponse<T> error(String errorMessage, String errorCode) {
        return new APIResponse<>(false, null, errorMessage, errorCode);
    }

}
