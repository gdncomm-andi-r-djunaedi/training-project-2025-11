package org.edmund.apigateway.services;

import jakarta.servlet.http.HttpServletRequest;
import org.edmund.apigateway.dto.ResolvedRoute;
import org.springframework.http.ResponseEntity;

public interface ReverseProxyService {

  ResponseEntity<byte[]> forward(HttpServletRequest request,
                                 byte[] body,
                                 ResolvedRoute resolvedRoute);
}
