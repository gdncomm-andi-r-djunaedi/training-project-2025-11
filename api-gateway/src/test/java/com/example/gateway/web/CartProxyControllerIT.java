package com.example.gateway.web;

import com.example.common.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
class CartProxyControllerIT {

  @LocalServerPort
  int port;

  @Autowired
  JwtService jwtService;

  @Autowired
  TestRestTemplate restTemplate;

  @BeforeEach
  void setupWiremock() {
    stubFor(post(urlPathMatching("/api/cart/.*"))
        .willReturn(aResponse().withStatus(200).withBody("OK")));
  }

  @Test
  void addCart_forwardsToCartServiceWithValidJwt() {
    String token = jwtService.generateToken("123", java.util.Map.of(), 3600);

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    HttpEntity<Void> entity = new HttpEntity<>(null, headers);

    ResponseEntity<String> resp = restTemplate.exchange(
        "http://localhost:" + port + "/cart/ABC?qty=1",
        HttpMethod.POST,
        entity,
        String.class);

    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(postRequestedFor(urlPathEqualTo("/api/cart/ABC")));
  }
}
