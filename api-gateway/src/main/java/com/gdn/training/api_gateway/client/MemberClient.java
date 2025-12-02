package com.gdn.training.api_gateway.client;

import java.time.Duration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import com.gdn.training.api_gateway.config.ServiceClientConfig;
import com.gdn.training.api_gateway.config.ServiceClientsProperties;
import com.gdn.training.api_gateway.dto.LoginRequest;
import com.gdn.training.api_gateway.dto.RegisterRequest;
import com.gdn.training.api_gateway.dto.UserInfoDTO;
import com.gdn.training.common.model.BaseResponse;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
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
        log.debug("Calling Member Service validate-credentials endpoint at {} for {}", url, request.getEmail());
        try {
            ResponseEntity<BaseResponse<UserInfoDTO>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(request),
                    new ParameterizedTypeReference<>() {}
            );

            BaseResponse<UserInfoDTO> body = response.getBody();
            if (body == null || !body.isSuccess() || body.getData() == null) {
                log.error("Member service responded with an unexpected payload for {}", request.getEmail());
                throw new IllegalStateException("Unable to process member service response");
            }

            log.debug("Member service validated credentials for {}", request.getEmail());
            return body.getData();
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().is4xxClientError()) {
                log.warn("Member service rejected credentials for {} with status {} and body {}",
                        request.getEmail(), ex.getStatusCode(), ex.getResponseBodyAsString());
                throw new IllegalArgumentException("Invalid credentials");
            }

            log.error("Member service returned {} for {} with body {}",
                    ex.getStatusCode(), request.getEmail(), ex.getResponseBodyAsString());
            throw new IllegalStateException("Member service is unavailable right now", ex);
        } catch (RestClientException ex) {
            log.error("Failed to reach member service for {}: {}", request.getEmail(), ex.getMessage());
            throw new IllegalStateException("Unable to validate credentials at the moment", ex);
        }
    }

    public void register(RegisterRequest request) {
        ServiceClientConfig config = serviceClientsProperties.getRequired("member");
        String url = config.getBaseUrl() + config.getEndpoints().get("register");
        log.debug("Calling Member Service register endpoint at {} for {}", url, request.getEmail());
        try {
            ResponseEntity<BaseResponse<Void>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(request),
                    new ParameterizedTypeReference<>() {}
            );

            BaseResponse<Void> body = response.getBody();
            if (body == null || !body.isSuccess()) {
                log.error("Member service returned an unsuccessful response for {}", request.getEmail());
                throw new IllegalStateException("Unable to register user right now");
            }

            log.info("Member service registered new user {}", request.getEmail());
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().is4xxClientError()) {
                log.warn("Member service rejected registration for {} with status {} and body {}",
                        request.getEmail(), ex.getStatusCode(), ex.getResponseBodyAsString());
                throw new IllegalArgumentException("Unable to register user with the provided data");
            }

            log.error("Member service returned {} for {} with body {}",
                    ex.getStatusCode(), request.getEmail(), ex.getResponseBodyAsString());
            throw new IllegalStateException("Member service is unavailable right now", ex);
        } catch (RestClientException ex) {
            log.error("Failed to reach member service for {}: {}", request.getEmail(), ex.getMessage());
            throw new IllegalStateException("Unable to register user at the moment", ex);
        }
    }


}
