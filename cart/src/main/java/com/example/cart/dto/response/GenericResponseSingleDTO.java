package com.example.cart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenericResponseSingleDTO<T> {
    private Integer statusCode;
    private String statusMessage;
    private T response;
}

