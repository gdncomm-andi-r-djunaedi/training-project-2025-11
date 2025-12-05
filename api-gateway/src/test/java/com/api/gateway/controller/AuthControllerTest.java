package com.api.gateway.controller;

import com.api.gateway.dto.request.LoginRequest;
import com.api.gateway.dto.request.RegisterRequest;
import com.api.gateway.dto.response.CustomerLoginResponse;
import com.api.gateway.dto.response.LoginResponse;
import com.api.gateway.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Date;

import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

@WebFluxTest(AuthController.class)
@Import(ObjectMapper.class)
class AuthControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @MockBean
    private ReactiveStringRedisTemplate redisTemplate;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private ObjectMapper objectMapper;

    private final String baseUrl = "http://localhost:8081";

    @BeforeEach
    void setup() {
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
    }


    /** ---------------------------------------------------
     *   REGISTER SUCCESS TEST
     * --------------------------------------------------- */
    @Test
    void testRegister_Success() throws Exception {

        RegisterRequest req = new RegisterRequest(
                "Test", "mail@test.com", "pass", "123", "addr"
        );

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);

        when(requestBodyUriSpec.uri(baseUrl + "/api/customers/createNewCustomer"))
                .thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON))
                .thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(req))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.bodyToMono(Object.class))
                .thenReturn(Mono.just(req));

        when(objectMapper.writerWithDefaultPrettyPrinter())
                .thenReturn(new ObjectMapper().writerWithDefaultPrettyPrinter());

        webTestClient.post()
                .uri("/api-gateway/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("mail@test.com"));
    }

    /** ---------------------------------------------------
     *   REGISTER ERROR TEST
     * --------------------------------------------------- */
    @Test
    void testRegister_Error() {

        RegisterRequest req = new RegisterRequest(
                "Test", "mail@test.com", "pass", "123", "addr"
        );

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);

        when(requestBodyUriSpec.uri(baseUrl + "/api/customers/createNewCustomer"))
                .thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON))
                .thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(req))
                .thenReturn(requestHeadersSpec);

        when(requestHeadersSpec.retrieve())
                .thenThrow(new RuntimeException("Server Down"));

        webTestClient.post()
                .uri("/api-gateway/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("Customer service unreachable"));
    }

    /** ---------------------------------------------------
     *   LOGIN SUCCESS TEST
     * --------------------------------------------------- */
    @Test
    void testLogin_Success() {

        LoginRequest req = new LoginRequest();
        req.setEmail("mail@test.com");
        req.setPassword("pass");

        CustomerLoginResponse mockResp =
                new CustomerLoginResponse();
        mockResp.setCustomerId("CUST123");
        mockResp.setEmail("mail@test.com");

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);

        when(requestBodyUriSpec.uri(baseUrl + "/internal/auth/login"))
                .thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(req)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(CustomerLoginResponse.class))
                .thenReturn(Mono.just(mockResp));

        when(jwtUtil.generateToken("CUST123", "mail@test.com"))
                .thenReturn("FAKE_TOKEN");

        webTestClient.post()
                .uri("/api-gateway/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectBody(LoginResponse.class)
                .value(resp -> {
                    assert resp.getCustomerId().equals("CUST123");
                    assert resp.getEmail().equals("mail@test.com");
                    assert resp.getToken().equals("FAKE_TOKEN");
                });
    }

    /** ---------------------------------------------------
     *   LOGOUT SUCCESS TEST
     * --------------------------------------------------- */
    @Test
    void testLogout_Success() {

        String token = "ABC.TOKEN.123";

        Date exp = new Date(System.currentTimeMillis() + 30000); // expires in 30s

        when(jwtUtil.getExpiration(token)).thenReturn(exp);

        when(redisTemplate.opsForValue()
                .set("BLACKLIST:" + token, "1", Duration.ofMillis(30000)))
                .thenReturn(Mono.just(true));

        webTestClient.post()
                .uri("/api-gateway/auth/logout")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("Logged out successfully"));
    }

    /** ---------------------------------------------------
     *   LOGOUT - NO HEADER
     * --------------------------------------------------- */
    @Test
    void testLogout_NoHeader() {

        webTestClient.post()
                .uri("/api-gateway/auth/logout")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("No token found"));

    }
}
