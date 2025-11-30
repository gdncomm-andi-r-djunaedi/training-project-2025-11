package com.gdn.training.api_gateway.client;

import com.gdn.training.api_gateway.config.ServiceClientConfig;
import com.gdn.training.api_gateway.config.ServiceClientsProperties;
import com.gdn.training.api_gateway.dto.RegisterRequest;
import com.gdn.training.api_gateway.dto.UserInfoDTO;
import com.gdn.training.api_gateway.dto.LoginRequest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Component
public class MemberClient {

    private final RestTemplate restTemplate;
    private final ServiceClientsProperties serviceClientsProperties;

    public MemberClient(RestTemplateBuilder builder, ServiceClientsProperties serviceClientsProperties) {
        this.serviceClientsProperties = serviceClientsProperties;
        ServiceClientConfig config = serviceClientsProperties.getRequired("member");
        
        this.restTemplate = builder
                .connectTimeout(Duration.ofMillis(config.getConnectTimeout()))
                .readTimeout(Duration.ofMillis(config.getReadTimeout()))
                .build();
    }

    public UserInfoDTO validateCredentials(LoginRequest request) {
        ServiceClientConfig config = serviceClientsProperties.getRequired("member");
        String url = config.getBaseUrl() + config.getEndpoints().get("validate-credentials");
        return restTemplate.postForObject(url, request, UserInfoDTO.class);
    }

}
