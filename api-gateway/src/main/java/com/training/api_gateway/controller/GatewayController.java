package com.training.api_gateway.controller;

import com.training.api_gateway.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class GatewayController {
  @Autowired
  private JwtService jwtService;

  @Autowired
  private RestTemplate restTemplate;

  @Value("${service.member.url}")
  private String memberServiceUrl;

  @Value("${service.product.url}")
  private String productServiceUrl;

  @Value("${service.cart.url}")
  private String cartServiceUrl;

  @RequestMapping("/api/auth/**")
  public ResponseEntity<String> routeAuth(
      HttpServletRequest request,
      @RequestBody(required = false) String body) {

    return forwardRequest(memberServiceUrl, request, body, null);
  }

  @RequestMapping("/api/products/**")
  public ResponseEntity<String> routeProducts(
      HttpServletRequest request,
      @RequestBody(required = false) String body) {

    return forwardRequest(productServiceUrl, request, body, null);
  }

  @RequestMapping("/api/cart/**")
  public ResponseEntity<String> routeCart(
      HttpServletRequest request,
      @RequestBody(required = false) String body) {

    String userEmail = validateAndExtractEmail(request);
    if (userEmail == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body("{\"error\":\"Unauthorized\"}");
    }

    return forwardRequest(cartServiceUrl, request, body, userEmail);
  }

  private String validateAndExtractEmail(HttpServletRequest request) {
    String authHeader = request.getHeader("Authorization");

    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      String token = authHeader.substring(7);

      if (jwtService.validateToken(token)) {
        return jwtService.extractEmail(token);
      }
    }

    return null;
  }

  private ResponseEntity<String> forwardRequest(
      String serviceUrl,
      HttpServletRequest request,
      String body,
      String userEmail) {

    String path = request.getRequestURI();
    String targetUrl = serviceUrl + path;

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    if (userEmail != null) {
      headers.set("X-Customer-Email", userEmail);
    }

    HttpEntity<String> entity = new HttpEntity<>(body, headers);
    HttpMethod method = HttpMethod.valueOf(request.getMethod());

    return restTemplate.exchange(targetUrl, method, entity, String.class);
  }
}
