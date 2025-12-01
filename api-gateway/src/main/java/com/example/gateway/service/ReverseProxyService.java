package com.example.gateway.service;

import com.example.gateway.model.ResolvedRoute;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

public interface ReverseProxyService {

  ResponseEntity<byte[]> forward(HttpServletRequest request,
                                 byte[] body,
                                 ResolvedRoute resolvedRoute);
}
