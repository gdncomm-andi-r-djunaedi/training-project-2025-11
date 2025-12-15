package com.example.gateway.command;

import com.example.commandlib.Command;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

public class ProxyPostCommand implements Command<ResponseEntity<?>> {
    private final RestTemplate restTemplate;
    private final String url;
    private final Object body;
    private final HttpHeaders headers;

    public ProxyPostCommand(RestTemplate restTemplate, String url, Object body) {
        this(restTemplate, url, body, null);
    }

    public ProxyPostCommand(RestTemplate restTemplate, String url, Object body, HttpHeaders headers) {
        this.restTemplate = restTemplate;
        this.url = url;
        this.body = body;
        this.headers = headers != null ? headers : new HttpHeaders();
    }

    @Override
    public ResponseEntity<?> execute() {
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);
        return restTemplate.postForEntity(url, entity, Object.class);
    }
}

