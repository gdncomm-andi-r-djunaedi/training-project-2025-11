package ProductService.ProductService.exception;

import ProductService.ProductService.common.ApiResponse;
import ProductService.ProductService.common.ResponseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(ProductNotFoundException.class)
        public ApiResponse<?> handleProductNotFound(ProductNotFoundException ex){
            return ResponseUtil.error(ex.getMessage(), "404 Not Found");
        }

       @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiResponse<?>> handleAllExceptions(Exception ex){
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseUtil.error(ex.getMessage(), "Error"));
    }

}
