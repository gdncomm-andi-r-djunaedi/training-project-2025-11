package com.example.apigateway.webclient;

import com.example.apigateway.security.UserPrincipal;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class UserIdForwardingInterceptor implements ClientHttpRequestInterceptor {

    private static final String USER_ID_HEADER = "X-User-Id";

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request, 
            byte[] body, 
            ClientHttpRequestExecution execution) throws IOException {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            String userId = ((UserPrincipal) authentication.getPrincipal()).getUserId();
            if (userId != null) {
                request.getHeaders().add(USER_ID_HEADER, userId);
            }
        }
        
        return execution.execute(request, body);
    }
}
