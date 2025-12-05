package com.elfrida.cart;

import com.elfrida.cart.configuration.JwtUtil;
import com.elfrida.cart.model.Cart;
import com.elfrida.cart.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class CartIntegrationTests {

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7.0")
            .withReuse(true);

    @DynamicPropertySource
    static void mongoProps(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
        registry.add("jwt.secret", () -> "superSecretKeyForHS256DontShare12345!");
        registry.add("jwt.expiration", () -> "86400000");
    }

    @LocalServerPort
    int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        cartRepository.deleteAll();
    }

    @Test
    void addViewAndDeleteCartItems_withJwtBearerToken() {
        String baseUrl = "http://localhost:" + port + "/cart";
        String token = jwtUtil.generateToken("testuser@example.com");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        // Add
        ResponseEntity<Cart> addResponse = restTemplate.exchange(
                baseUrl + "/items?productId=P001&quantity=2",
                HttpMethod.POST,
                new HttpEntity<>(null, headers),
                Cart.class);

        // View
        ResponseEntity<Cart> getResponse = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                new HttpEntity<>(null, headers),
                Cart.class);

        // Delete
        ResponseEntity<Cart> deleteResponse = restTemplate.exchange(
                baseUrl + "/items?productId=P001",
                HttpMethod.DELETE,
                new HttpEntity<>(null, headers),
                Cart.class);

        assertThat(addResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().getItems()).hasSize(1);
        assertThat(deleteResponse.getBody().getItems()).isEmpty();
    }
}
