package com.ecom.cart.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Optional;

@Data
public class ApiResponse<T> implements Serializable {

    private int code;
    private String status;
    private T data;

    public ApiResponse(int code, String status, T data) {
        this.code = code;
        this.data = data;
        this.status = status;
    }

    public static <T> ApiResponse<T> success(int code, T data) {
        return new ApiResponse<>(code, "SUCCESS", data);
    }

    public static <T> ApiResponse<T> error(int code, T data) {
        return new ApiResponse<>(code, "ERROR", data);
    }


    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public T getData() {
        return data;
    }
}
