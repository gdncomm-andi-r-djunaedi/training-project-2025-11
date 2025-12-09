package com.gdn.project.waroenk.gateway.controller;

import com.gdn.project.waroenk.gateway.exception.RouteNotFoundException;
import com.gdn.project.waroenk.gateway.exception.ServiceUnavailableException;
import com.gdn.project.waroenk.gateway.service.GrpcProxyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GatewayController.class)
@DisplayName("GatewayController Unit Tests")
class GatewayControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private GrpcProxyService grpcProxyService;

  @Nested
  @DisplayName("handleGet Tests")
  class HandleGetTests {

    @Test
    @WithMockUser
    @DisplayName("Should handle GET request successfully")
    void shouldHandleGetRequestSuccessfully() throws Exception {
      // Given
      String expectedResponse = "{\"id\":\"123\",\"name\":\"Test User\"}";
      when(grpcProxyService.invoke(eq("GET"), eq("/api/users"), isNull(), anyMap(), any(), any()))
          .thenReturn(expectedResponse);

      // When/Then
      mockMvc.perform(get("/api/users"))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(content().json(expectedResponse));
    }

    @Test
    @WithMockUser
    @DisplayName("Should handle GET request with query params")
    void shouldHandleGetRequestWithQueryParams() throws Exception {
      // Given
      String expectedResponse = "{\"users\":[]}";
      when(grpcProxyService.invoke(eq("GET"), eq("/api/users"), isNull(), anyMap(), any(), any()))
          .thenReturn(expectedResponse);

      // When/Then
      mockMvc.perform(get("/api/users")
              .param("page", "0")
              .param("size", "10"))
          .andExpect(status().isOk());
    }
  }

  @Nested
  @DisplayName("handlePost Tests")
  class HandlePostTests {

    @Test
    @WithMockUser
    @DisplayName("Should handle POST request with JSON body")
    void shouldHandlePostRequestWithJsonBody() throws Exception {
      // Given
      String requestBody = "{\"name\":\"New User\",\"email\":\"test@example.com\"}";
      String expectedResponse = "{\"id\":\"new-123\",\"message\":\"Created\"}";
      when(grpcProxyService.invoke(eq("POST"), eq("/api/users"), eq(requestBody), anyMap(), any(), any()))
          .thenReturn(expectedResponse);

      // When/Then
      mockMvc.perform(post("/api/users")
              .with(csrf())
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody))
          .andExpect(status().isOk())
          .andExpect(content().json(expectedResponse));
    }
  }

  @Nested
  @DisplayName("handlePut Tests")
  class HandlePutTests {

    @Test
    @WithMockUser
    @DisplayName("Should handle PUT request")
    void shouldHandlePutRequest() throws Exception {
      // Given
      String requestBody = "{\"name\":\"Updated User\"}";
      String expectedResponse = "{\"success\":true}";
      when(grpcProxyService.invoke(eq("PUT"), eq("/api/users/123"), eq(requestBody), anyMap(), any(), any()))
          .thenReturn(expectedResponse);

      // When/Then
      mockMvc.perform(put("/api/users/123")
              .with(csrf())
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody))
          .andExpect(status().isOk())
          .andExpect(content().json(expectedResponse));
    }
  }

  @Nested
  @DisplayName("handlePatch Tests")
  class HandlePatchTests {

    @Test
    @WithMockUser
    @DisplayName("Should handle PATCH request")
    void shouldHandlePatchRequest() throws Exception {
      // Given
      String requestBody = "{\"status\":\"active\"}";
      String expectedResponse = "{\"success\":true}";
      when(grpcProxyService.invoke(eq("PATCH"), eq("/api/users/123"), eq(requestBody), anyMap(), any(), any()))
          .thenReturn(expectedResponse);

      // When/Then
      mockMvc.perform(patch("/api/users/123")
              .with(csrf())
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody))
          .andExpect(status().isOk())
          .andExpect(content().json(expectedResponse));
    }
  }

  @Nested
  @DisplayName("handleDelete Tests")
  class HandleDeleteTests {

    @Test
    @WithMockUser
    @DisplayName("Should handle DELETE request")
    void shouldHandleDeleteRequest() throws Exception {
      // Given
      String expectedResponse = "{\"success\":true}";
      when(grpcProxyService.invoke(eq("DELETE"), eq("/api/users/123"), isNull(), anyMap(), any(), any()))
          .thenReturn(expectedResponse);

      // When/Then
      mockMvc.perform(delete("/api/users/123")
              .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(content().json(expectedResponse));
    }
  }

  @Nested
  @DisplayName("Error Handling Tests")
  class ErrorHandlingTests {

    @Test
    @WithMockUser
    @DisplayName("Should propagate RouteNotFoundException")
    void shouldPropagateRouteNotFoundException() throws Exception {
      // Given
      when(grpcProxyService.invoke(anyString(), anyString(), any(), anyMap(), any(), any()))
          .thenThrow(new RouteNotFoundException("Route not found"));

      // When/Then - Exception will be handled by ControllerAdvice
      mockMvc.perform(get("/api/nonexistent"))
          .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    @DisplayName("Should propagate ServiceUnavailableException")
    void shouldPropagateServiceUnavailableException() throws Exception {
      // Given
      when(grpcProxyService.invoke(anyString(), anyString(), any(), anyMap(), any(), any()))
          .thenThrow(new ServiceUnavailableException("Service unavailable"));

      // When/Then - Exception will be handled by ControllerAdvice
      mockMvc.perform(get("/api/unavailable"))
          .andExpect(status().isServiceUnavailable());
    }
  }
}

