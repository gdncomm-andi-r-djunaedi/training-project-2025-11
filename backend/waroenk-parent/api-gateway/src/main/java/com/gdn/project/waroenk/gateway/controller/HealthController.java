package com.gdn.project.waroenk.gateway.controller;

import com.gdn.project.waroenk.gateway.config.GatewayProperties;
import com.gdn.project.waroenk.gateway.config.GrpcChannelConfig;
import com.gdn.project.waroenk.gateway.service.StaticRouteRegistry;
import com.gdn.project.waroenk.gateway.service.StaticRouteRegistry.RouteInfo;
import com.gdn.project.waroenk.gateway.service.StaticRouteRegistry.ServiceInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Health check, status, and service information controller.
 * Simplified for static route configuration.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Health & Info", description = "Health check and gateway information endpoints")
public class HealthController {

    private final GatewayProperties gatewayProperties;
    private final GrpcChannelConfig channelConfig;
    private final StaticRouteRegistry routeRegistry;

    @Value("${info.app.version:1.0.0}")
    private String appVersion;

    @Value("${spring.application.name:api-gateway}")
    private String appName;

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Returns the health status of the gateway")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    @Operation(summary = "Application info", description = "Returns information about the gateway")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("name", appName);
        response.put("version", appVersion);
        response.put("mode", "static");
        response.put("timestamp", LocalDateTime.now());

        // Route stats
        Map<String, Object> routeStats = new LinkedHashMap<>();
        routeStats.put("total", routeRegistry.getRouteCount());
        routeStats.put("source", "application.properties");
        response.put("routes", routeStats);

        // Service stats
        Map<String, Object> serviceStats = new LinkedHashMap<>();
        serviceStats.put("total", routeRegistry.getServiceCount());
        serviceStats.put("connected", channelConfig.getActiveServiceNames().size());
        response.put("services", serviceStats);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/services")
    @Operation(summary = "List services", description = "Returns all configured services")
    public ResponseEntity<Map<String, Object>> services() {
        Map<String, Object> response = new LinkedHashMap<>();

        List<Map<String, Object>> serviceList = new ArrayList<>();
        for (ServiceInfo service : routeRegistry.getAllServices()) {
            Map<String, Object> serviceInfo = new LinkedHashMap<>();
            serviceInfo.put("name", service.name());
            serviceInfo.put("host", service.host());
            serviceInfo.put("port", service.port());
            serviceInfo.put("use_tls", service.useTls());
            serviceInfo.put("connected", channelConfig.hasChannel(service.name()));
            serviceList.add(serviceInfo);
        }
        response.put("services", serviceList);
        response.put("total", serviceList.size());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/routes")
    @Operation(summary = "List routes", description = "Returns all configured routes")
    public ResponseEntity<Map<String, Object>> routes() {
        Map<String, Object> response = new LinkedHashMap<>();

        List<Map<String, Object>> routeList = new ArrayList<>();
        for (RouteInfo route : routeRegistry.getAllRoutes()) {
            Map<String, Object> routeInfo = new LinkedHashMap<>();
            routeInfo.put("http_method", route.httpMethod());
            routeInfo.put("path", route.httpPath());
            routeInfo.put("service", route.serviceName());
            routeInfo.put("grpc_service", route.grpcServiceName());
            routeInfo.put("grpc_method", route.grpcMethodName());
            routeInfo.put("public", route.publicEndpoint());
            routeList.add(routeInfo);
        }
        response.put("routes", routeList);
        response.put("total", routeList.size());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/routes/summary")
    @Operation(summary = "Routes summary", description = "Returns route count grouped by service")
    public ResponseEntity<Map<String, Object>> routesSummary() {
        Map<String, Object> response = new LinkedHashMap<>();

        // Group routes by service
        Map<String, Long> routesByService = routeRegistry.getAllRoutes().stream()
                .collect(Collectors.groupingBy(
                        RouteInfo::serviceName,
                        Collectors.counting()
                ));

        List<Map<String, Object>> summary = new ArrayList<>();
        for (Map.Entry<String, Long> entry : routesByService.entrySet()) {
            Map<String, Object> serviceRoutes = new LinkedHashMap<>();
            serviceRoutes.put("service", entry.getKey());
            serviceRoutes.put("routes", entry.getValue());
            summary.add(serviceRoutes);
        }

        response.put("by_service", summary);
        response.put("total_services", routesByService.size());
        response.put("total_routes", routeRegistry.getRouteCount());

        return ResponseEntity.ok(response);
    }
}
