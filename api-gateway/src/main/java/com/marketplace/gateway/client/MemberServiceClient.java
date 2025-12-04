package com.marketplace.gateway.client;

import com.marketplace.common.dto.ApiResponse;
import com.marketplace.common.dto.UserDetailsResponse;
import com.marketplace.common.dto.ValidateCredentialsRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * HTTP client for communicating with Member Service
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MemberServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${services.member.url:http://localhost:8081}")
    private String memberServiceUrl;

    /**
     * Validate user credentials via Member Service
     */
    public Mono<UserDetailsResponse> validateCredentials(ValidateCredentialsRequest request) {
        log.info("Validating credentials for user: {}", request.getUsername());

        return webClientBuilder.build()
                .post()
                .uri(memberServiceUrl + "/api/member/validate-credentials")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ApiResponse.class)
                .map(response -> {
                    // Extract UserDetailsResponse from ApiResponse
                    Object data = response.getData();
                    if (data instanceof UserDetailsResponse) {
                        return (UserDetailsResponse) data;
                    }
                    // Handle LinkedHashMap case (Jackson deserialization)
                    return convertToUserDetailsResponse(data);
                })
                .doOnSuccess(user -> log.info("Credentials validated successfully for user: {}", user.getUsername()))
                .doOnError(error -> log.error("Credential validation failed: {}", error.getMessage()));
    }

    @SuppressWarnings("unchecked")
    private UserDetailsResponse convertToUserDetailsResponse(Object data) {
        if (data instanceof java.util.Map) {
            java.util.Map<String, Object> map = (java.util.Map<String, Object>) data;
            return UserDetailsResponse.builder()
                    .id(java.util.UUID.fromString((String) map.get("id")))
                    .username((String) map.get("username"))
                    .email((String) map.get("email"))
                    .fullName((String) map.get("fullName"))
                    .roles((java.util.List<String>) map.get("roles"))
                    .build();
        }
        throw new IllegalArgumentException("Unable to convert response data to UserDetailsResponse");
    }
}
