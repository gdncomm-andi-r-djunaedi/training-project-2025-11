package com.gdn.project.waroenk.gateway.service;

import com.gdn.project.waroenk.gateway.config.GrpcChannelConfig;
import com.gdn.project.waroenk.gateway.exception.GatewayException;
import com.gdn.project.waroenk.gateway.exception.RouteNotFoundException;
import com.gdn.project.waroenk.gateway.exception.ServiceUnavailableException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import io.grpc.Channel;
import io.grpc.MethodDescriptor;
import io.grpc.StatusRuntimeException;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.ClientCalls;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Service that dynamically invokes gRPC methods based on route configuration.
 * This is the core of the agnostic gateway - it doesn't know about specific services,
 * but uses configuration to route requests to the correct gRPC methods.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GrpcProxyService {

    private final GrpcServiceRegistry serviceRegistry;
    private final RouteResolver routeResolver;
    private final GrpcChannelConfig channelConfig;

    // Bounded cache for method descriptors with TTL (prevents memory leak)
    private final Cache<String, MethodDescriptor<Message, Message>> methodDescriptorCache = Caffeine.newBuilder()
            .maximumSize(500)                    // Max 500 method descriptors
            .expireAfterAccess(Duration.ofHours(1)) // Expire after 1 hour of no access
            .recordStats()                       // Enable stats for monitoring
            .build();

    @PostConstruct
    public void init() {
        // Set the route resolver on the channel config for dynamic channel creation
        channelConfig.setRouteResolver(routeResolver);
    }

    /**
     * Invoke a gRPC method based on HTTP request parameters
     *
     * @param httpMethod HTTP method (GET, POST, etc.)
     * @param path       HTTP path
     * @param jsonBody   Request body as JSON (can be null for GET requests)
     * @param queryParams Query parameters (for GET requests)
     * @return Response as JSON string
     */
    public String invoke(String httpMethod, String path, String jsonBody, Map<String, String> queryParams) {
        // Resolve the route
        RouteResolver.ResolvedRoute route = routeResolver.resolve(httpMethod, path)
                .orElseThrow(() -> new RouteNotFoundException(
                        "No microservice registered for " + httpMethod + " " + path));

        log.debug("Resolved route: {} {} -> {}.{}", httpMethod, path, route.grpcServiceName(), route.grpcMethodName());

        try {
            // Extract path variables from the URL using the matched pattern
            Map<String, String> pathVariables = new HashMap<>();
            if (route.httpPathPattern() != null) {
                try {
                    pathVariables = routeResolver.extractPathVariables(route.httpPathPattern(), path);
                    log.debug("Extracted path variables: {}", pathVariables);
                } catch (Exception e) {
                    log.debug("No path variables to extract for pattern: {}", route.httpPathPattern());
                }
            }
            
            // Merge path variables with query params (path variables take precedence)
            Map<String, String> allParams = new HashMap<>();
            if (queryParams != null) {
                allParams.putAll(queryParams);
            }
            allParams.putAll(pathVariables);
            
            // Build request message
            Message requestMessage = buildRequestMessage(route, jsonBody, allParams);
            
            // Get channel and invoke
            Channel channel = channelConfig.getChannel(route.serviceName());
            Message responseMessage = invokeGrpc(channel, route, requestMessage);
            
            // Convert response to JSON
            return serviceRegistry.messageToJson(responseMessage);
            
        } catch (StatusRuntimeException e) {
            log.error("gRPC call failed: {}", e.getStatus());
            throw e; // Let the controller advice handle it
        } catch (InvalidProtocolBufferException e) {
            log.error("Failed to parse/serialize protobuf: {}", e.getMessage());
            throw new GatewayException("Failed to process request/response", e);
        } catch (IllegalArgumentException e) {
            log.error("Service not found: {}", e.getMessage());
            throw new ServiceUnavailableException("Service " + route.serviceName() + " is not available");
        } catch (Exception e) {
            log.error("Unexpected error during gRPC invocation: {}", e.getMessage(), e);
            throw new GatewayException("Failed to invoke service: " + e.getMessage(), e);
        }
    }

    private Message buildRequestMessage(RouteResolver.ResolvedRoute route, String jsonBody, Map<String, String> queryParams) 
            throws InvalidProtocolBufferException {
        
        String requestType = route.requestType();
        
        if (requestType == null || requestType.isBlank()) {
            // No request type specified, try to use common.Empty or common.Id based on query params
            if (queryParams != null && queryParams.containsKey("id")) {
                requestType = "com.gdn.project.waroenk.common.Id";
                jsonBody = "{\"value\": \"" + queryParams.get("id") + "\"}";
            } else {
                requestType = "com.gdn.project.waroenk.common.Empty";
                jsonBody = "{}";
            }
        }

        // If no body provided, try to build from query params
        if ((jsonBody == null || jsonBody.isBlank()) && queryParams != null && !queryParams.isEmpty()) {
            jsonBody = buildJsonFromQueryParams(queryParams);
        }

        // Default to empty JSON if still null
        if (jsonBody == null || jsonBody.isBlank()) {
            jsonBody = "{}";
        }

        return serviceRegistry.parseJsonToMessage(jsonBody, requestType);
    }

    private String buildJsonFromQueryParams(Map<String, String> queryParams) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            if (!first) {
                json.append(",");
            }
            first = false;
            
            // Convert to snake_case as proto uses snake_case
            String key = camelToSnake(entry.getKey());
            String value = entry.getValue();
            
            // Try to detect if value is numeric or boolean
            if (value.matches("-?\\d+(\\.\\d+)?")) {
                json.append("\"").append(key).append("\":").append(value);
            } else if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                json.append("\"").append(key).append("\":").append(value.toLowerCase());
            } else {
                json.append("\"").append(key).append("\":\"").append(escapeJson(value)).append("\"");
            }
        }
        
        json.append("}");
        return json.toString();
    }

    private String camelToSnake(String str) {
        return str.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }

    private Message invokeGrpc(Channel channel, RouteResolver.ResolvedRoute route, Message request) {
        // Build method descriptor (using bounded Caffeine cache)
        String methodKey = route.grpcServiceName() + "/" + route.grpcMethodName();
        MethodDescriptor<Message, Message> methodDescriptor = methodDescriptorCache.get(
                methodKey,
                k -> buildMethodDescriptor(route, request)
        );

        try {
            // Make blocking unary call
            return ClientCalls.blockingUnaryCall(
                    channel,
                    methodDescriptor,
                    io.grpc.CallOptions.DEFAULT,
                    request
            );
        } catch (io.grpc.StatusRuntimeException e) {
            if (e.getStatus().getCode() == io.grpc.Status.Code.UNAVAILABLE) {
                throw new ServiceUnavailableException("Service " + route.serviceName() + " is unavailable");
            }
            throw e;
        }
    }

    private MethodDescriptor<Message, Message> buildMethodDescriptor(RouteResolver.ResolvedRoute route, Message requestMessage) {
        String fullMethodName = route.grpcServiceName() + "/" + route.grpcMethodName();
        
        // Get response type
        String responseType = route.responseType();
        Message responseDefault;
        
        if (responseType != null && !responseType.isBlank()) {
            responseDefault = serviceRegistry.getDefaultInstance(responseType);
        } else {
            // Try to infer response type from method name
            responseDefault = inferResponseType(route);
        }

        return MethodDescriptor.<Message, Message>newBuilder()
                .setType(MethodDescriptor.MethodType.UNARY)
                .setFullMethodName(fullMethodName)
                .setRequestMarshaller(ProtoUtils.marshaller(requestMessage.getDefaultInstanceForType()))
                .setResponseMarshaller(ProtoUtils.marshaller(responseDefault))
                .build();
    }

    private Message inferResponseType(RouteResolver.ResolvedRoute route) {
        // Try common response types based on method name patterns
        String methodName = route.grpcMethodName().toLowerCase();
        
        if (methodName.contains("delete") || methodName.contains("remove") || methodName.contains("clear")) {
            return serviceRegistry.getDefaultInstance("com.gdn.project.waroenk.common.Basic");
        }
        
        // Default to Basic response
        return serviceRegistry.getDefaultInstance("com.gdn.project.waroenk.common.Basic");
    }
}
