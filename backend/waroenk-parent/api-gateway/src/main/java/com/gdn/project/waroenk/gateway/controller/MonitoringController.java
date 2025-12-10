package com.gdn.project.waroenk.gateway.controller;

import com.gdn.project.waroenk.gateway.dto.monitoring.DashboardSummaryDto;
import com.gdn.project.waroenk.gateway.dto.monitoring.ServiceHealthDto;
import com.gdn.project.waroenk.gateway.dto.monitoring.ServiceInfoDto;
import com.gdn.project.waroenk.gateway.dto.monitoring.ServiceMetricsDto;
import com.gdn.project.waroenk.gateway.service.MonitoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for monitoring gateway services.
 * Provides health check and status information for all configured services.
 * 
 * Health checks run automatically every 30 seconds.
 * Metrics are collected every 60 seconds.
 */
@RestController
@RequestMapping("/monitoring")
@RequiredArgsConstructor
@Tag(name = "Monitoring", description = "Service monitoring and health check endpoints")
public class MonitoringController {

    private final MonitoringService monitoringService;

    @GetMapping("/summary")
    @Operation(summary = "Get dashboard summary", description = "Returns health status summary for all services (for dashboard)")
    public ResponseEntity<DashboardSummaryDto> getSummary() {
        return ResponseEntity.ok(monitoringService.getDashboardSummary());
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard summary (alias)", description = "Returns health status summary for all services")
    public ResponseEntity<DashboardSummaryDto> getDashboard() {
        return ResponseEntity.ok(monitoringService.getDashboardSummary());
    }

    @GetMapping("/services")
    @Operation(summary = "Get all services health", description = "Returns health status for all configured services")
    public ResponseEntity<List<ServiceHealthDto>> getAllServicesHealth() {
        return ResponseEntity.ok(monitoringService.getAllServicesHealth());
    }

    @GetMapping("/services/{serviceName}")
    @Operation(summary = "Get service info", description = "Returns detailed info for a specific service")
    public ResponseEntity<ServiceInfoDto> getServiceInfo(@PathVariable String serviceName) {
        return ResponseEntity.ok(monitoringService.getServiceInfo(serviceName));
    }

    @GetMapping("/services/{serviceName}/info")
    @Operation(summary = "Get service info (alias)", description = "Returns detailed info for a specific service")
    public ResponseEntity<ServiceInfoDto> getServiceInfoAlias(@PathVariable String serviceName) {
        return ResponseEntity.ok(monitoringService.getServiceInfo(serviceName));
    }

    @GetMapping("/services/{serviceName}/health")
    @Operation(summary = "Get service health", description = "Returns health status for a specific service")
    public ResponseEntity<ServiceHealthDto> getServiceHealth(@PathVariable String serviceName) {
        return monitoringService.getServiceHealth(serviceName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/services/{serviceName}/metrics")
    @Operation(summary = "Get service metrics", description = "Returns current metrics for a specific service from actuator")
    public ResponseEntity<ServiceMetricsDto> getServiceMetrics(@PathVariable String serviceName) {
        return ResponseEntity.ok(monitoringService.getServiceMetrics(serviceName));
    }

    @GetMapping("/services/{serviceName}/metrics/history")
    @Operation(summary = "Get service metrics history", description = "Returns historical metrics for trending (up to 60 data points)")
    public ResponseEntity<List<ServiceMetricsDto>> getServiceMetricsHistory(@PathVariable String serviceName) {
        return ResponseEntity.ok(monitoringService.getServiceMetricsHistory(serviceName));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Force health check refresh", description = "Triggers immediate health check for all services")
    public ResponseEntity<Map<String, String>> forceRefresh() {
        monitoringService.forceHealthCheck();
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "message", "Health check triggered for all services"
        ));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get route statistics", description = "Returns statistics about configured routes")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(monitoringService.getRouteStats());
    }
}
