package com.ecom.member.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {

    private int code;
    private String status;
    private String message;
    private T data;

    public static ApiResponse success(int code, String message, Object data) {
        return new ApiResponse(code, "SUCCESS", message, data);
    }

    public static ApiResponse error(int code, String message) {
        return new ApiResponse(code, "ERROR", message, "[]");
    }
}
