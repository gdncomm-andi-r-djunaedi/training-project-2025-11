package com.blibli.search.exception;

import com.blibli.search.dto.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Global Exception Handler Tests")
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;



    @Test
    @DisplayName("Should handle BadRequestException correctly")
    void handleBadRequestException_Success() {

        String errorMessage = "Invalid request parameter";
        BadRequestException exception = new BadRequestException(errorMessage);

        ResponseEntity<ApiResponse<Void>> response = globalExceptionHandler.handleBadRequestException(exception);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo(errorMessage);
    }



    @Test
    @DisplayName("Should handle SearchException correctly")
    void handleSearchException_Success() {

        String errorMessage = "Search operation failed";
        SearchException exception = new SearchException(errorMessage);


        ResponseEntity<ApiResponse<Void>> response = globalExceptionHandler.handleSearchException(exception);


        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).contains("Search operation failed");
    }

    @Test
    @DisplayName("Should handle SearchException with cause correctly")
    void handleSearchException_Success_WithCause() {

        String errorMessage = "Search operation failed";
        RuntimeException cause = new RuntimeException("Elasticsearch connection error");
        SearchException exception = new SearchException(errorMessage, cause);

        ResponseEntity<ApiResponse<Void>> response = globalExceptionHandler.handleSearchException(exception);


        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).contains("Search operation failed");
    }


    @Test
    @DisplayName("Should handle ElasticsearchException correctly")
    void handleElasticsearchException_Success() {

        String errorMessage = "Elasticsearch connection failed";
        ElasticsearchException exception = new ElasticsearchException(errorMessage);


        ResponseEntity<ApiResponse<Void>> response = globalExceptionHandler.handleElasticsearchException(exception);


        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).contains("Elasticsearch service error");
    }

    @Test
    @DisplayName("Should handle ElasticsearchException with cause correctly")
    void handleElasticsearchException_Success_WithCause() {

        String errorMessage = "Elasticsearch connection failed";
        RuntimeException cause = new RuntimeException("Connection timeout");
        ElasticsearchException exception = new ElasticsearchException(errorMessage, cause);


        ResponseEntity<ApiResponse<Void>> response = globalExceptionHandler.handleElasticsearchException(exception);


        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).contains("Elasticsearch service error");
    }



    @Test
    @DisplayName("Should handle MethodArgumentNotValidException correctly")
    void handleValidationExceptions_Success() {
        // Given
        BindingResult bindingResult = mock(BindingResult.class);
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);

        List<FieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(new FieldError("productRequest", "name", "Name is required"));
        fieldErrors.add(new FieldError("productRequest", "price", "Price must be positive"));

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(new ArrayList<>(fieldErrors));

        // When
        ResponseEntity<ApiResponse<java.util.Map<String, String>>> response =
                globalExceptionHandler.handleValidationExceptions(exception);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("Validation failed");
        assertThat(response.getBody().getData()).isNotNull();
        assertThat(response.getBody().getData().size()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException with empty errors")
    void handleValidationExceptions_Success_EmptyErrors() {
        // Given
        BindingResult bindingResult = mock(BindingResult.class);
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(new ArrayList<>());

        // When
        ResponseEntity<ApiResponse<java.util.Map<String, String>>> response =
                globalExceptionHandler.handleValidationExceptions(exception);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getData()).isNotNull();
        assertThat(response.getBody().getData().isEmpty()).isTrue();
    }



    @Test
    @DisplayName("Should handle IllegalArgumentException correctly")
    void handleIllegalArgumentException_Success() {
        // Given
        String errorMessage = "Invalid argument value";
        IllegalArgumentException exception = new IllegalArgumentException(errorMessage);


        ResponseEntity<ApiResponse<Void>> response = globalExceptionHandler.handleIllegalArgumentException(exception);


        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).contains("Invalid request");
    }



    @Test
    @DisplayName("Should handle generic Exception correctly")
    void handleGenericException_Success() {
        // Given
        RuntimeException exception = new RuntimeException("Unexpected error occurred");

        // When
        ResponseEntity<ApiResponse<Void>> response = globalExceptionHandler.handleGenericException(exception);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred. Please try again later.");
    }

    @Test
    @DisplayName("Should handle NullPointerException correctly")
    void handleGenericException_Success_NullPointerException() {
        // Given
        NullPointerException exception = new NullPointerException("Null value encountered");

        // When
        ResponseEntity<ApiResponse<Void>> response = globalExceptionHandler.handleGenericException(exception);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred. Please try again later.");
    }
}

