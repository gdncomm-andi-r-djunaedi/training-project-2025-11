
package com.zasura.product.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CommonResponse Tests")
class CommonResponseTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create CommonResponse with no-args constructor")
        void testNoArgsConstructor() {
            // When
            CommonResponse<String> response = new CommonResponse<>();

            // Then
            assertNotNull(response);
            assertNull(response.getCode());
            assertNull(response.getStatus());
            assertNull(response.getSuccess());
            assertNull(response.getData());
            assertNull(response.getErrorMessage());
            assertNull(response.getPagination());
        }

        @Test
        @DisplayName("Should create CommonResponse with all-args constructor")
        void testAllArgsConstructor() {
            // Given
            Integer code = 200;
            String status = "OK";
            Boolean success = true;
            String data = "Test Data";
            Object errorMessage = null;
            Pagination pagination = new Pagination(0, 10);

            // When
            CommonResponse<String> response = new CommonResponse<>(code, status, success, data, errorMessage,
                    pagination);

            // Then
            assertNotNull(response);
            assertEquals(code, response.getCode());
            assertEquals(status, response.getStatus());
            assertEquals(success, response.getSuccess());
            assertEquals(data, response.getData());
            assertNull(response.getErrorMessage());
            assertEquals(pagination, response.getPagination());
        }

        @Test
        @DisplayName("Should create CommonResponse with builder")
        void testBuilder() {
            // Given
            Integer code = 201;
            String status = "CREATED";
            Boolean success = true;
            String data = "Created Data";
            Pagination pagination = new Pagination(1, 20);

            // When
            CommonResponse<String> response = CommonResponse.<String>builder()
                    .code(code)
                    .status(status)
                    .success(success)
                    .data(data)
                    .pagination(pagination)
                    .build();

            // Then
            assertNotNull(response);
            assertEquals(code, response.getCode());
            assertEquals(status, response.getStatus());
            assertEquals(success, response.getSuccess());
            assertEquals(data, response.getData());
            assertNull(response.getErrorMessage());
            assertEquals(pagination, response.getPagination());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should set and get code")
        void testCodeGetterSetter() {
            // Given
            CommonResponse<String> response = new CommonResponse<>();
            Integer code = 404;

            // When
            response.setCode(code);

            // Then
            assertEquals(code, response.getCode());
        }

        @Test
        @DisplayName("Should set and get status")
        void testStatusGetterSetter() {
            // Given
            CommonResponse<String> response = new CommonResponse<>();
            String status = "NOT_FOUND";

            // When
            response.setStatus(status);

            // Then
            assertEquals(status, response.getStatus());
        }

        @Test
        @DisplayName("Should set and get success")
        void testSuccessGetterSetter() {
            // Given
            CommonResponse<String> response = new CommonResponse<>();
            Boolean success = false;

            // When
            response.setSuccess(success);

            // Then
            assertEquals(success, response.getSuccess());
        }

        @Test
        @DisplayName("Should set and get data")
        void testDataGetterSetter() {
            // Given
            CommonResponse<String> response = new CommonResponse<>();
            String data = "Sample Data";

            // When
            response.setData(data);

            // Then
            assertEquals(data, response.getData());
        }

        @Test
        @DisplayName("Should set and get errorMessage")
        void testErrorMessageGetterSetter() {
            // Given
            CommonResponse<String> response = new CommonResponse<>();
            String errorMessage = "Error occurred";

            // When
            response.setErrorMessage(errorMessage);

            // Then
            assertEquals(errorMessage, response.getErrorMessage());
        }

        @Test
        @DisplayName("Should set and get pagination")
        void testPaginationGetterSetter() {
            // Given
            CommonResponse<String> response = new CommonResponse<>();
            Pagination pagination = new Pagination(2, 15);

            // When
            response.setPagination(pagination);

            // Then
            assertEquals(pagination, response.getPagination());
        }
    }

    @Nested
    @DisplayName("Static Factory Method Tests")
    class StaticFactoryMethodTests {

        @Test
        @DisplayName("Should create success response with data")
        void testSuccessMethod() {
            // Given
            String data = "Success Data";

            // When
            CommonResponse<String> response = CommonResponse.success(data);

            // Then
            assertNotNull(response);
            assertEquals(HttpStatus.OK.value(), response.getCode());
            assertEquals(HttpStatus.OK.name(), response.getStatus());
            assertTrue(response.getSuccess());
            assertEquals(data, response.getData());
            assertNull(response.getErrorMessage());
            assertNull(response.getPagination());
        }

        @Test
        @DisplayName("Should create success response with null data")
        void testSuccessMethodWithNullData() {
            // When
            CommonResponse<String> response = CommonResponse.success(null);

            // Then
            assertNotNull(response);
            assertEquals(HttpStatus.OK.value(), response.getCode());
            assertEquals(HttpStatus.OK.name(), response.getStatus());
            assertTrue(response.getSuccess());
            assertNull(response.getData());
            assertNull(response.getErrorMessage());
            assertNull(response.getPagination());
        }

        @Test
        @DisplayName("Should create success response with complex data type")
        void testSuccessMethodWithComplexData() {
            // Given
            List<String> data = Arrays.asList("Item1", "Item2", "Item3");

            // When
            CommonResponse<List<String>> response = CommonResponse.success(data);

            // Then
            assertNotNull(response);
            assertEquals(HttpStatus.OK.value(), response.getCode());
            assertEquals(HttpStatus.OK.name(), response.getStatus());
            assertTrue(response.getSuccess());
            assertEquals(data, response.getData());
            assertEquals(3, response.getData().size());
        }

        @Test
        @DisplayName("Should create success response with pagination")
        void testSuccessWithPaginationMethod() {
            // Given
            String data = "Paginated Data";
            Pagination pagination = new Pagination(0, 10);

            // When
            CommonResponse<String> response = CommonResponse.successWithPagination(data, pagination);

            // Then
            assertNotNull(response);
            assertEquals(HttpStatus.OK.value(), response.getCode());
            assertEquals(HttpStatus.OK.name(), response.getStatus());
            assertTrue(response.getSuccess());
            assertEquals(data, response.getData());
            assertNull(response.getErrorMessage());
            assertNotNull(response.getPagination());
            assertEquals(0, response.getPagination().getPage());
            assertEquals(10, response.getPagination().getSize());
        }

        @Test
        @DisplayName("Should create success response with pagination and null data")
        void testSuccessWithPaginationMethodNullData() {
            // Given
            Pagination pagination = new Pagination(1, 20);

            // When
            CommonResponse<String> response = CommonResponse.successWithPagination(null, pagination);

            // Then
            assertNotNull(response);
            assertEquals(HttpStatus.OK.value(), response.getCode());
            assertEquals(HttpStatus.OK.name(), response.getStatus());
            assertTrue(response.getSuccess());
            assertNull(response.getData());
            assertNotNull(response.getPagination());
            assertEquals(1, response.getPagination().getPage());
            assertEquals(20, response.getPagination().getSize());
        }

        @Test
        @DisplayName("Should create success response with null pagination")
        void testSuccessWithPaginationMethodNullPagination() {
            // Given
            String data = "Data without pagination";

            // When
            CommonResponse<String> response = CommonResponse.successWithPagination(data, null);

            // Then
            assertNotNull(response);
            assertEquals(HttpStatus.OK.value(), response.getCode());
            assertEquals(HttpStatus.OK.name(), response.getStatus());
            assertTrue(response.getSuccess());
            assertEquals(data, response.getData());
            assertNull(response.getPagination());
        }

        @Test
        @DisplayName("Should create success response with list data and pagination")
        void testSuccessWithPaginationMethodListData() {
            // Given
            List<String> data = Arrays.asList("Item1", "Item2", "Item3", "Item4", "Item5");
            Pagination pagination = new Pagination(0, 5);

            // When
            CommonResponse<List<String>> response = CommonResponse.successWithPagination(data, pagination);

            // Then
            assertNotNull(response);
            assertEquals(HttpStatus.OK.value(), response.getCode());
            assertEquals(HttpStatus.OK.name(), response.getStatus());
            assertTrue(response.getSuccess());
            assertEquals(data, response.getData());
            assertEquals(5, response.getData().size());
            assertNotNull(response.getPagination());
            assertEquals(0, response.getPagination().getPage());
            assertEquals(5, response.getPagination().getSize());
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle error response with error message")
        void testErrorResponse() {
            // Given
            String errorMessage = "An error occurred";

            // When
            CommonResponse<String> response = CommonResponse.<String>builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .status(HttpStatus.BAD_REQUEST.name())
                    .success(false)
                    .errorMessage(errorMessage)
                    .build();

            // Then
            assertNotNull(response);
            assertEquals(HttpStatus.BAD_REQUEST.value(), response.getCode());
            assertEquals(HttpStatus.BAD_REQUEST.name(), response.getStatus());
            assertFalse(response.getSuccess());
            assertNull(response.getData());
            assertEquals(errorMessage, response.getErrorMessage());
        }

        @Test
        @DisplayName("Should handle error response with complex error message")
        void testErrorResponseWithComplexErrorMessage() {
            // Given
            List<String> errors = Arrays.asList("Error 1", "Error 2", "Error 3");

            // When
            CommonResponse<String> response = CommonResponse.<String>builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .status(HttpStatus.BAD_REQUEST.name())
                    .success(false)
                    .errorMessage(errors)
                    .build();

            // Then
            assertNotNull(response);
            assertEquals(HttpStatus.BAD_REQUEST.value(), response.getCode());
            assertFalse(response.getSuccess());
            assertNotNull(response.getErrorMessage());
            assertTrue(response.getErrorMessage() instanceof List);
        }

        @Test
        @DisplayName("Should handle response with all null values")
        void testAllNullValues() {
            // When
            CommonResponse<String> response = CommonResponse.<String>builder().build();

            // Then
            assertNotNull(response);
            assertNull(response.getCode());
            assertNull(response.getStatus());
            assertNull(response.getSuccess());
            assertNull(response.getData());
            assertNull(response.getErrorMessage());
            assertNull(response.getPagination());
        }

        @Test
        @DisplayName("Should handle different generic types")
        void testDifferentGenericTypes() {
            // Test with Integer
            CommonResponse<Integer> intResponse = CommonResponse.success(42);
            assertEquals(42, intResponse.getData());

            // Test with Boolean
            CommonResponse<Boolean> boolResponse = CommonResponse.success(true);
            assertTrue(boolResponse.getData());

            // Test with Double
            CommonResponse<Double> doubleResponse = CommonResponse.success(3.14);
            assertEquals(3.14, doubleResponse.getData());
        }
    }
}