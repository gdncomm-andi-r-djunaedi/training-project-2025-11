package com.blublu.cart.exception;

import com.blublu.cart.model.response.GenericBodyResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ErrorHandlerTest {

    @InjectMocks
    private ErrorHandler errorHandler;

    @Test
    void dataNotFoundHandler_ShouldReturnInternalServerError() {
        String errorMessage = "Product not found";
        ProductNotFoundException exception = new ProductNotFoundException(errorMessage);

        ResponseEntity<GenericBodyResponse> response = errorHandler.dataNotFoundHandler(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        GenericBodyResponse body = response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertEquals(errorMessage, body.getErrorMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), body.getErrorCode());
    }
}
