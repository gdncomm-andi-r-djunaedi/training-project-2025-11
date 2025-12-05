package com.blublu.member.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class ErrorHandlerTest {

    private final ErrorHandler errorHandler = new ErrorHandler();

    @Test
    void testUsernameExistHandler() {
        UsernameExistException ex = new UsernameExistException("User exists");
        ResponseEntity<Map<String, Object>> response = errorHandler.dataNotFoundHandler(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User exists", response.getBody().get("errorMessage"));
    }

    @Test
    void testUsernameNotExistHandler() {
        UsernameNotExistException ex = new UsernameNotExistException("User not found");
        ResponseEntity<Map<String, Object>> response = errorHandler.dataNotFoundHandler(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User not found", response.getBody().get("errorMessage"));
    }

    @Test
    void testWrongPasswordHandler() {
        WrongPasswordException ex = new WrongPasswordException("Wrong password");
        ResponseEntity<Map<String, Object>> response = errorHandler.wrongPasswordHandler(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Wrong password", response.getBody().get("errorMessage"));
    }
}
