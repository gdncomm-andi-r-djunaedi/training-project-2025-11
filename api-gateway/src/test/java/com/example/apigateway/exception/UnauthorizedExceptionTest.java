package com.example.apigateway.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UnauthorizedExceptionTest {

  @Test
  void shouldCreateAndThrowExceptionWithMessage() {
    String message = "Unauthorized access";

    UnauthorizedException exception = assertThrows(
        UnauthorizedException.class,
        () -> {
          throw new UnauthorizedException(message);
        });

    assertEquals(message, exception.getMessage());
  }
}

