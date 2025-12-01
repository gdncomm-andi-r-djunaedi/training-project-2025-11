package com.example.gateway.service.impl;

import com.example.gateway.model.ResolvedRoute;
import com.example.gateway.service.ReverseProxyService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReverseProxyServiceImpl implements ReverseProxyService {

  private final RestTemplate restTemplate;

  @Override
  public ResponseEntity<byte[]> forward(HttpServletRequest request, byte[] body, ResolvedRoute resolvedRoute) {

    String requestUri = request.getRequestURI();
    String queryString = request.getQueryString();

    var def = resolvedRoute.definition();

    // downstream path = targetPathPrefix + remainingPath
    String downstreamPath = def.targetPathPrefix() + resolvedRoute.remainingPath();

    String targetUrl = def.targetBaseUrl() + downstreamPath;
    if (queryString != null && !queryString.isBlank()) {
      targetUrl += "?" + queryString;
    }

    HttpMethod method = HttpMethod.valueOf(request.getMethod());
    HttpHeaders headers = extractHeaders(request);

    // inject userId for authenticated routes
    if (def.requiresAuth()) {
      Object userId = request.getAttribute("userId");
      if (userId != null) {
        headers.set("X-User-Id", userId.toString());
      }
    }

    HttpEntity<byte[]> entity = new HttpEntity<>(body, headers);

    try {
      ResponseEntity<byte[]> downstreamResponse =
          restTemplate.exchange(URI.create(targetUrl), method, entity, byte[].class);

      HttpHeaders responseHeaders = copySafeHeaders(downstreamResponse.getHeaders());

      return new ResponseEntity<>(downstreamResponse.getBody(), responseHeaders, downstreamResponse.getStatusCode());
    } catch (HttpStatusCodeException ex) {
      // in case someone later removes the custom error handler, we still behave as a good proxy
      HttpHeaders responseHeaders = copySafeHeaders(ex.getResponseHeaders());

      return new ResponseEntity<>(ex.getResponseBodyAsByteArray(), responseHeaders, ex.getStatusCode());
    }
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

  /**
   * Copy only end-to-end headers, drop hop-by-hop headers
   * that can break chunking / framing in the proxy response.
   */
  private HttpHeaders copySafeHeaders(HttpHeaders source) {
    HttpHeaders target = new HttpHeaders();
    if (source == null) {
      return target;
    }

    source.forEach((name, values) -> {
      if (!name.equalsIgnoreCase(HttpHeaders.TRANSFER_ENCODING) && !name.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH)
          && !name.equalsIgnoreCase(HttpHeaders.CONNECTION)) {
        target.put(name, values);
      }
    });

    return target;
  }
}