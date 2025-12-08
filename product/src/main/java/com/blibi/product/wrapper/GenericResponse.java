package com.blibi.product.wrapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GenericResponse<T> {

    private boolean success;

    private String message;

    private T data;

    public static <T> GenericResponse<T> success(T data, String message) {
        return new GenericResponse<>(true, message, data);
    }

    public static <T> GenericResponse<T> error(String message) {
        return new GenericResponse<>(false, message, null);
    }
}
