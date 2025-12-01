package com.example.gateway.web;

import com.example.gateway.routing.RouteDefinition;
import com.example.gateway.routing.RouteRegistry;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProxyController {

  private final RouteRegistry routeRegistry;
  private final RestTemplate restTemplate;

  @RequestMapping({"/api/members/**", "/api/products/**", "/api/cart/**"})
  public ResponseEntity<byte[]> proxy(HttpServletRequest request, @RequestBody(required = false) byte[] body) {

    String requestUri = request.getRequestURI();
    String queryString = request.getQueryString();

    RouteDefinition route = routeRegistry.findRoute(requestUri);

    if (route == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(("No route for path: " + requestUri).getBytes());
    }

    // Strip path prefix
    String downstreamPath = requestUri.substring(route.pathPrefix().length());
    String targetUrl = route.targetBaseUrl() + route.targetPathPrefix() + downstreamPath;

    if (queryString != null && !queryString.isBlank()) {
      targetUrl += "?" + queryString;
    }

    HttpMethod method = HttpMethod.valueOf(request.getMethod());
    HttpHeaders headers = extractHeaders(request);

    // inject userId for authenticated routes
    if (route.requiresAuth()) {
      Object userId = request.getAttribute("userId");
      if (userId != null)
        headers.set("X-User-Id", userId.toString());
    }

    HttpEntity<byte[]> entity = new HttpEntity<>(body, headers);

    ResponseEntity<byte[]> responseEntity = restTemplate.exchange(URI.create(targetUrl), method, entity, byte[].class);

    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.putAll(responseEntity.getHeaders());

    return new ResponseEntity<>(responseEntity.getBody(), responseHeaders, responseEntity.getStatusCode());
  }

  private HttpHeaders extractHeaders(HttpServletRequest request) {
    HttpHeaders headers = new HttpHeaders();
    Enumeration<String> headerNames = request.getHeaderNames();

    if (headerNames != null) {
      while (headerNames.hasMoreElements()) {
        String headerName = headerNames.nextElement();
        if (!headerName.equalsIgnoreCase(HttpHeaders.HOST)) {
          List<String> values = Collections.list(request.getHeaders(headerName));
          headers.put(headerName, values);
        }
      }
    }
    return headers;
  }
}