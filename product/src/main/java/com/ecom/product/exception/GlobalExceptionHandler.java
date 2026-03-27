package com.ecom.product.exception;

import com.ecom.product.Dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler{

    @ExceptionHandler(NoDataFoundException.class)
    public ApiResponse NoResultFoundException(NoDataFoundException ex){
        return ApiResponse.error(404, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse exception(Exception ex){
        System.out.println(ex.getMessage());
        return ApiResponse.error(500, ex.getMessage());
    }

}
