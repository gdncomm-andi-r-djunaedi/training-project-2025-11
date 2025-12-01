package com.example.gateway.controller;

import com.example.gateway.model.ResolvedRoute;
import com.example.gateway.service.ReverseProxyService;
import com.example.gateway.service.RouteRegistryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProxyController {

  private final RouteRegistryService routeRegistry;
  private final ReverseProxyService reverseProxyService;

  @RequestMapping("/api/**")
  public ResponseEntity<byte[]> proxy(HttpServletRequest request, @RequestBody(required = false) byte[] body) {

    String requestUri = request.getRequestURI(); // e.g. /api/members/register

    ResolvedRoute resolved = routeRegistry.resolve(requestUri);

    if (resolved == null) {
      String msg = "No route for path: " + requestUri;
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(msg.getBytes());
    }

    return reverseProxyService.forward(request, body, resolved);
  }
}