package com.training.member.memberassignment.common;

public class BaseResponse<T>
{
    private boolean success;
    private String code;
    private String message;
    private T data;

    private BaseResponse(boolean success, String code, String message, T data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(true, "SUCCESS", null, data);
    }

    public static <T> BaseResponse<T> error(String code, String message) {
        return new BaseResponse<>(false, code, message, null);
    }
}