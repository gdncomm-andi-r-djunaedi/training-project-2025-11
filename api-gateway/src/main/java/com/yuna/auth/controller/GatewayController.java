package com.yuna.auth.controller;

import com.yuna.auth.model.User;
import com.yuna.auth.properties.Item;
import com.yuna.auth.properties.ServiceProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/**")
public class GatewayController {

  @Autowired
  private RestClient restClient;

  @Autowired
  private ServiceProperties serviceProperties;

  @RequestMapping
  public Object forward(HttpServletRequest request) throws Exception {
    String originalPath = request.getRequestURI();
    Optional<String> targetService = resolveTargetService(originalPath);

    if (targetService.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(Map.of("error", originalPath + "Route not found"));
    }

    String forwardedPath = originalPath;

    // 🔥 Forward query params
    String queryString = Optional.ofNullable(request.getQueryString()).orElse("");
    String finalUrl = targetService.get() + forwardedPath + "?" + queryString;

    String body = readBody(request);
    HttpHeaders headers = extractHeaders(request);
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User currentUser = (User) authentication.getPrincipal();
    headers.add("username", currentUser.getUsername());

    RestClient.RequestBodySpec builder = restClient
        .method(HttpMethod.valueOf(request.getMethod()))
        .uri(finalUrl)
        .headers(h -> h.addAll(headers));

    if (!body.isEmpty()) {
      builder = builder.body(body);
    }

    return builder.retrieve()
        .toEntity(Object.class);
  }

  private HttpHeaders extractHeaders(HttpServletRequest req) {
    HttpHeaders headers = new HttpHeaders();
    Collections.list(req.getHeaderNames())
        .forEach(name -> headers.addAll(name, Collections.list(req.getHeaders(name))));
    return headers;
  }

  private Optional<String> resolveTargetService(String path) {
    return serviceProperties.getClients().stream().filter(x -> path.startsWith(x.getPrefix())).findFirst().map(Item::getHost);
  }

  private String readBody(HttpServletRequest request) throws IOException {
    try (InputStream is = request.getInputStream()) {
      return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }
  }
}
