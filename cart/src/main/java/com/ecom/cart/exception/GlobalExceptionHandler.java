package com.ecom.cart.exception;

import com.ecom.cart.Dto.ApiResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoDataFoundException.class)
    public ApiResponse NoDataFoundException(NoDataFoundException ex){
        return ApiResponse.error(404, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse exception(Exception e){
        return ApiResponse.error(500, e.getMessage());
    }

}
