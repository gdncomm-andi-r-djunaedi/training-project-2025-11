package com.example.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenericResponseListDTO<T> {
    private Integer statusCode;
    private String statusMessage;
    private List<T> response;
}
