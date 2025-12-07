package com.example.apigateway.client;

import com.example.apigateway.dto.LoginRequest;
import com.example.apigateway.dto.MemberValidationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MemberClientTest {

    private MockWebServer mockWebServer;
    private MemberClient memberClient;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        objectMapper = new ObjectMapper();

        // Configure MemberClient to use the mock server's URL
        String baseUrl = mockWebServer.url("/").toString();
        // Remove trailing slash if present (though WebClient handles it, strictly
        // following logic)
        // logic in client is just .baseUrl(memberServiceBaseUrl)

        // We need to inject WebClient.Builder and the base URL via Reflection or
        // constructor if feasible
        // But MemberClient uses @Value.
        // Best approach for unit test: manually instantiate MemberClient with a real
        // WebClient.Builder and set the field value via Reflection if needed,
        // OR easier since we are unit testing: just instantiate it and use
        // ReflectionTestUtils to set the private field.

        WebClient.Builder webClientBuilder = WebClient.builder();
        memberClient = new MemberClient(webClientBuilder);
        org.springframework.test.util.ReflectionTestUtils.setField(memberClient, "memberServiceBaseUrl", baseUrl);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void validateCredentials_shouldReturnMemberResponse_whenCredentialsAreValid() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("user", "password");
        MemberValidationResponse expectedResponse = new MemberValidationResponse(1L, "user");

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(expectedResponse))
                .addHeader("Content-Type", "application/json"));

        // Act
        Mono<MemberValidationResponse> result = memberClient.validateCredentials(loginRequest);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> response.getUserId().equals(1L) &&
                        response.getUsername().equals("user"))
                .verifyComplete();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("/auth/login", recordedRequest.getPath());
        assertEquals(objectMapper.writeValueAsString(loginRequest), recordedRequest.getBody().readUtf8());
    }
}
