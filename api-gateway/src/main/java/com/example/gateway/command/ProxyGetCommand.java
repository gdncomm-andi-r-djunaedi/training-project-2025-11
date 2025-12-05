package com.example.gateway.command;

import com.example.commandlib.Command;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

public class ProxyGetCommand implements Command<ResponseEntity<?>> {
    private final RestTemplate restTemplate;
    private final String url;
    private final HttpHeaders headers;

    public ProxyGetCommand(RestTemplate restTemplate, String url) {
        this(restTemplate, url, null);
    }

    public ProxyGetCommand(RestTemplate restTemplate, String url, HttpHeaders headers) {
        this.restTemplate = restTemplate;
        this.url = url;
        this.headers = headers != null ? headers : new HttpHeaders();
    }

    @Override
    public ResponseEntity<?> execute() {
        HttpEntity<Object> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, entity, Object.class);
    }
}

