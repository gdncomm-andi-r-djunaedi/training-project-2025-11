package com.blibli.gatewayModule.filter;

import com.blibli.gatewayModule.util.JwtUtil;
import com.blibli.memberModule.dto.ApiResponse;
import com.blibli.memberModule.dto.LoginResponseDto;
import com.blibli.memberModule.dto.MemberResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class LoginResponseFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestPath = exchange.getRequest().getURI().getPath();

        if (!requestPath.equals("/api/members/login")) {
            return chain.filter(exchange);
        }

        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().set("Content-Type", "application/json");

        ServerHttpResponseDecorator responseWrapper = createResponseWrapper(response);

        return chain.filter(exchange.mutate().response(responseWrapper).build());
    }

    private ServerHttpResponseDecorator createResponseWrapper(ServerHttpResponse response) {
        return new ServerHttpResponseDecorator(response) {
            @Override
            public Mono<Void> writeWith(org.reactivestreams.Publisher<? extends DataBuffer> body) {
                if (body == null) {
                    return super.writeWith(null);
                }

                Flux<DataBuffer> responseStream = Flux.from(body);
                return DataBufferUtils.join(responseStream).flatMap(dataBuffer -> {
                    if (dataBuffer == null || dataBuffer.readableByteCount() == 0) {
                        return super.writeWith(body);
                    }

                    String responseText = readResponseAsText(dataBuffer);
                    String responseWithToken = addJwtTokenToResponse(responseText);
                    return writeResponse(response, responseWithToken);
                }).onErrorResume(error -> {
                    log.error("Error processing login response", error);
                    return super.writeWith(body);
                });
            }
        };
    }

    private String readResponseAsText(DataBuffer dataBuffer) {
        byte[] bytes = new byte[dataBuffer.readableByteCount()];
        dataBuffer.read(bytes);
        DataBufferUtils.release(dataBuffer);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private Mono<Void> writeResponse(ServerHttpResponse response, String responseText) {
        byte[] responseBytes = responseText.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(responseBytes);
        return response.writeWith(Mono.just(buffer));
    }

    private String addJwtTokenToResponse(String responseText) {
        try {
            if (responseText == null || responseText.trim().isEmpty()) {
                log.warn("Empty response from member service");
                return responseText;
            }

            ApiResponse<LoginResponseDto> apiResponse = parseResponse(responseText);
            if (apiResponse == null || apiResponse.getValue() == null) {
                log.warn("Invalid response structure from member service");
                return responseText;
            }

            LoginResponseDto loginResponse = apiResponse.getValue();
            MemberResponseDto member = loginResponse.getMember();
            if (member == null) {
                log.warn("Member info not found in login response");
                return responseText;
            }

            Long memberId = member.getMemberId();
            String email = member.getEmail();

            if (memberId == null || email == null) {
                log.warn("MemberId or email is null");
                return responseText;
            }

            String jwtToken = jwtUtil.generateToken(memberId, email);
            loginResponse.setToken(jwtToken);

            log.info("JWT token generated for memberId: {}", memberId);

            return objectMapper.writeValueAsString(apiResponse);

        } catch (Exception e) {
            log.error("Error adding JWT token to login response", e);
            return responseText;
        }
    }

    private ApiResponse<LoginResponseDto> parseResponse(String responseText) {
        try {
            return objectMapper.readValue(responseText,
                    new com.fasterxml.jackson.core.type.TypeReference<ApiResponse<LoginResponseDto>>() {
                    });
        } catch (Exception e) {
            log.error("Error parsing response from member service", e);
            return null;
        }
    }

    @Override
    public int getOrder() {
        return -1;
    }
}