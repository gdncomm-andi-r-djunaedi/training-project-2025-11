package com.api.gateway.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.UUID;

import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@WebFluxTest(CartGatewayController.class)
@ExtendWith(MockitoExtension.class)
class CartGatewayControllerTest {

    @Mock
    private WebClient.Builder builder;

    @Mock
    private WebClient client;

    @Mock
    private WebClient clientAfterMutate;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Autowired
    private WebTestClient webTestClient;

    private final String baseUrl = "http://localhost:8082";

    @TestConfiguration
    static class TestConfig {
        @Bean
        public WebClient.Builder webClientBuilder() {
            return WebClient.builder();
        }
    }


    @BeforeEach
    void setup() {

        // --- builder.build()
        when(builder.build()).thenReturn(client);

        // --- client.mutate().defaultHeader().build()
        WebClient.Builder mutatedBuilder = org.mockito.Mockito.mock(WebClient.Builder.class);

        when(client.mutate()).thenReturn(mutatedBuilder);
        when(mutatedBuilder.defaultHeader(anyString(), anyString())).thenReturn(mutatedBuilder);
        when(mutatedBuilder.build()).thenReturn(clientAfterMutate);

        // --------------- DEFAULT MOCK CHAIN ---------------
        when(clientAfterMutate.get()).thenReturn(requestHeadersUriSpec);
        when(clientAfterMutate.post()).thenReturn(requestBodyUriSpec);
        when(clientAfterMutate.put()).thenReturn(requestBodyUriSpec);
        when(clientAfterMutate.method(any(HttpMethod.class))).thenReturn(requestBodyUriSpec);
        when(clientAfterMutate.delete()).thenReturn(requestHeadersUriSpec);

        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);

        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);

        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.bodyToMono(any(Class.class)))
                .thenReturn(Mono.just(Map.of("status", "ok")));
    }

    // -------------------------------------------------------
    // GET CART
    // -------------------------------------------------------
    @Test
    void testGetCart_Success() {
        UUID id = UUID.randomUUID();

        webTestClient.get()
                .uri("/gateway/cart/" + id)
                .header("Authorization", "Bearer abc")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("ok");
    }

    // -------------------------------------------------------
    // ADD ITEM
    // -------------------------------------------------------
    @Test
    void testAddItem_Success() {
        UUID id = UUID.randomUUID();

        webTestClient.post()
                .uri("/gateway/cart/" + id + "/add")
                .header("Authorization", "Bearer abc")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("item", "A"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("ok");
    }

    // -------------------------------------------------------
    // UPDATE ITEM
    // -------------------------------------------------------
    @Test
    void testUpdateItem_Success() {
        UUID id = UUID.randomUUID();

        webTestClient.put()
                .uri("/gateway/cart/" + id + "/update")
                .header("Authorization", "Bearer abc")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("item", "B"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("ok");
    }

    // -------------------------------------------------------
    // REMOVE ITEM
    // -------------------------------------------------------
    @Test
    void testRemoveItem_Success() {
        UUID id = UUID.randomUUID();

        webTestClient.method(HttpMethod.DELETE)
                .uri("/gateway/cart/" + id + "/remove")
                .header("Authorization", "Bearer abc")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("item", "C"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("ok");
    }

    // -------------------------------------------------------
    // CLEAR CART
    // -------------------------------------------------------
    @Test
    void testClearCart_Success() {
        UUID id = UUID.randomUUID();

        webTestClient.delete()
                .uri("/gateway/cart/" + id + "/clear")
                .header("Authorization", "Bearer abc")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("ok");
    }

    // -------------------------------------------------------
    // Missing token
    // -------------------------------------------------------
    @Test
    void testMissingToken() {
        UUID id = UUID.randomUUID();

        webTestClient.get()
                .uri("/gateway/cart/" + id)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // -------------------------------------------------------
    // Invalid token prefix
    // -------------------------------------------------------
    @Test
    void testInvalidToken() {
        UUID id = UUID.randomUUID();

        webTestClient.get()
                .uri("/gateway/cart/" + id)
                .header("Authorization", "Token 123")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
