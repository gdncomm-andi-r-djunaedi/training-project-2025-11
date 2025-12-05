package com.gdn.training.api_gateway.config;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ServiceClientConfig {

    private String baseUrl;

    private Map<String, String> endpoints = new HashMap<>();

    private long connectTimeout = 5000;
    private long readTimeout = 10000;
}
