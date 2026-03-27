package com.ecom.product.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

//@Data
//@AllArgsConstructor
@NoArgsConstructor
@Data
public class ApiResponse<T> implements Serializable {

    private int code;
    private String status;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

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
}

