package com.blibli.training.framework.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse<T> {
    private boolean success;
    private int code;
    private String message;
    private T data;
    
    public static <T> BaseResponse<T> success(T data) {
        return BaseResponse.<T>builder()
                .success(true)
                .code(200)
                .message("Success")
                .data(data)
                .build();
    }

    public static <T> BaseResponse<T> error(int code, String message) {
        return BaseResponse.<T>builder()
                .success(false)
                .code(code)
                .message(message)
                .build();
    }
}
