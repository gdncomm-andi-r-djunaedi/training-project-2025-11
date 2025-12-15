package org.edmund.apigateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Router {
    private String id;
    private String pathPrefix;
    private String targetBaseUrl;
    private String targetPathPrefix;
    private boolean requiresAuth;
}