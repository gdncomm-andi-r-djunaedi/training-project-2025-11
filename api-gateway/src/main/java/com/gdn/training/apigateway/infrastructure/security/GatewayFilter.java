package com.gdn.training.apigateway.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface GatewayFilter {
    /**
     * Handle the request and determine if it should proceed.
     * 
     * @param request  the HTTP request
     * @param response the HTTP response
     * @return true if the request should proceed, false otherwise
     * @throws IOException if an I/O error occurs
     */
    boolean handle(HttpServletRequest request, HttpServletResponse response) throws IOException;
}
