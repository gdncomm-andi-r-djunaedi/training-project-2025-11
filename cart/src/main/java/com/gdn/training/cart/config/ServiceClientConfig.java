package com.gdn.training.cart.config;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ServiceClientConfig {

    private String baseUrl;

    /**
     * key: endpoint name (detail, list, etc.)
     * value: endpoint path
     */
    private Map<String, String> endpoints = new HashMap<>();

    private long connectTimeout = 5000; // default 5s
    private long readTimeout = 10000;   // default 10s
}
