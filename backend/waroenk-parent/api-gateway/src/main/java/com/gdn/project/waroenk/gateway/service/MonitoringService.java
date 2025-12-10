package com.gdn.project.waroenk.gateway.service;

import com.gdn.project.waroenk.common.HealthResponse;
import com.gdn.project.waroenk.common.HealthStatus;
import com.gdn.project.waroenk.common.InfoResponse;
import com.gdn.project.waroenk.common.MetricsResponse;
import com.gdn.project.waroenk.gateway.config.GatewayProperties;
import com.gdn.project.waroenk.gateway.dto.monitoring.DashboardSummaryDto;
import com.gdn.project.waroenk.gateway.dto.monitoring.ServiceHealthDto;
import com.gdn.project.waroenk.gateway.dto.monitoring.ServiceInfoDto;
import com.gdn.project.waroenk.gateway.dto.monitoring.ServiceMetricsDto;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Monitoring service that uses gRPC to collect health and metrics from backend services.
 * 
 * Benefits of gRPC over HTTP:
 * - Reuses existing gRPC channels (no additional HTTP connections)
 * - Lower latency (binary protocol)
 * - Type-safe responses via proto definitions
 * - Consistent with the gateway's gRPC-first architecture
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitoringService {

    private final GatewayProperties gatewayProperties;
    private final GrpcHealthClient grpcHealthClient;
    private final StaticRouteRegistry routeRegistry;

    // Health check cache: serviceName -> latest health
    private final Map<String, ServiceHealthDto> healthCache = new ConcurrentHashMap<>();

    // Metrics history: serviceName -> list of metrics (bounded)
    private final Map<String, List<ServiceMetricsDto>> metricsHistory = new ConcurrentHashMap<>();

    // Maximum history entries per service
    private static final int MAX_HISTORY_SIZE = 60; // ~1 hour at 1 min intervals

    @PostConstruct
    public void init() {
        // Initialize health cache with UNKNOWN status
        gatewayProperties.getServices().forEach((name, config) -> {
            healthCache.put(name, ServiceHealthDto.builder()
                    .serviceName(name)
                    .host(config.getHost())
                    .grpcPort(config.getPort())
                    .httpPort(config.getHttpPort())
                    .status("UNKNOWN")
                    .source("grpc")
                    .lastCheck(LocalDateTime.now())
                    .healthDetails(Map.of("note", "Initial state, checking soon..."))
                    .build());
            metricsHistory.put(name, new CopyOnWriteArrayList<>());
        });

        log.info("MonitoringService initialized for {} services (using gRPC)", 
                gatewayProperties.getServices().size());
        
        // Run initial health check immediately
        checkAllServicesHealth();
    }

    /**
     * Scheduled health check - runs every 30 seconds.
     */
    @Scheduled(fixedRate = 30000, initialDelay = 5000)
    public void scheduledHealthCheck() {
        log.debug("Running scheduled gRPC health check...");
        checkAllServicesHealth();
    }

    /**
     * Scheduled metrics collection - runs every minute.
     */
    @Scheduled(fixedRate = 60000, initialDelay = 10000)
    public void scheduledMetricsCollection() {
        log.debug("Running scheduled gRPC metrics collection...");
        collectAllServicesMetrics();
    }

    /**
     * Check health of all configured services via gRPC.
     */
    public void checkAllServicesHealth() {
        gatewayProperties.getServices().forEach((serviceName, config) -> {
            CompletableFuture.runAsync(() -> {
                try {
                    HealthResponse grpcHealth = grpcHealthClient.getHealth(serviceName);
                    ServiceHealthDto health = convertHealthResponse(serviceName, config, grpcHealth);
                    healthCache.put(serviceName, health);
                    log.debug("Health check for {}: {}", serviceName, health.getStatus());
                } catch (Exception e) {
                    log.warn("Health check failed for {}: {}", serviceName, e.getMessage());
                    healthCache.put(serviceName, ServiceHealthDto.builder()
                            .serviceName(serviceName)
                            .host(config.getHost())
                            .grpcPort(config.getPort())
                            .httpPort(config.getHttpPort())
                            .status("DOWN")
                            .source("grpc")
                            .lastCheck(LocalDateTime.now())
                            .errorMessage(e.getMessage())
                            .build());
                }
            });
        });
    }

    /**
     * Collect metrics from all services via gRPC.
     */
    public void collectAllServicesMetrics() {
        gatewayProperties.getServices().forEach((serviceName, config) -> {
            CompletableFuture.runAsync(() -> {
                try {
                    MetricsResponse grpcMetrics = grpcHealthClient.getMetrics(serviceName);
                    ServiceMetricsDto metrics = convertMetricsResponse(serviceName, grpcMetrics);
                    addMetricsToHistory(serviceName, metrics);
                } catch (Exception e) {
                    log.debug("Metrics collection failed for {}: {}", serviceName, e.getMessage());
                }
            });
        });
    }

    /**
     * Convert gRPC HealthResponse to ServiceHealthDto.
     */
    private ServiceHealthDto convertHealthResponse(String serviceName, 
            GatewayProperties.ServiceConfig config, HealthResponse grpcHealth) {
        
        String status = switch (grpcHealth.getStatus()) {
            case UP -> "UP";
            case DOWN -> "DOWN";
            case DEGRADED -> "DEGRADED";
            default -> "UNKNOWN";
        };

        Map<String, Object> healthDetails = new LinkedHashMap<>();
        healthDetails.put("grpcStatus", grpcHealth.getStatus().name());
        
        // Add component health details
        if (!grpcHealth.getComponentsMap().isEmpty()) {
            Map<String, Object> components = new LinkedHashMap<>();
            grpcHealth.getComponentsMap().forEach((name, comp) -> {
                Map<String, Object> compDetails = new LinkedHashMap<>();
                compDetails.put("status", comp.getStatus().name());
                compDetails.putAll(comp.getDetailsMap());
                components.put(name, compDetails);
            });
            healthDetails.put("components", components);
        }

        return ServiceHealthDto.builder()
                .serviceName(serviceName)
                .host(config.getHost())
                .grpcPort(config.getPort())
                .httpPort(config.getHttpPort())
                .status(status)
                .source("grpc")
                .lastCheck(grpcHealth.getTimestamp() > 0 
                        ? LocalDateTime.ofInstant(Instant.ofEpochMilli(grpcHealth.getTimestamp()), ZoneId.systemDefault())
                        : LocalDateTime.now())
                .healthDetails(healthDetails)
                .errorMessage(grpcHealth.getErrorMessage().isEmpty() ? null : grpcHealth.getErrorMessage())
                .build();
    }

    /**
     * Convert gRPC MetricsResponse to ServiceMetricsDto.
     */
    private ServiceMetricsDto convertMetricsResponse(String serviceName, MetricsResponse grpcMetrics) {
        ServiceMetricsDto.MemoryMetrics memory = null;
        if (grpcMetrics.hasMemory()) {
            var m = grpcMetrics.getMemory();
            memory = ServiceMetricsDto.MemoryMetrics.builder()
                    .heapUsed(m.getHeapUsed())
                    .heapMax(m.getHeapMax())
                    .heapCommitted(m.getHeapCommitted())
                    .nonHeapUsed(m.getNonHeapUsed())
                    .nonHeapCommitted(m.getNonHeapCommitted())
                    .heapUsedPercentage(m.getHeapUsedPercentage())
                    .build();
        }

        ServiceMetricsDto.JvmMetrics jvm = null;
        if (grpcMetrics.hasJvm()) {
            var j = grpcMetrics.getJvm();
            jvm = ServiceMetricsDto.JvmMetrics.builder()
                    .threadCount(j.getThreadCount())
                    .threadPeakCount(j.getThreadPeakCount())
                    .threadDaemonCount(j.getThreadDaemonCount())
                    .classesLoaded(j.getClassesLoaded())
                    .cpuUsage(j.getCpuUsage())
                    .uptimeSeconds(j.getUptimeSeconds())
                    .gcPauseCount(j.getGcCount())
                    .gcPauseTime((double) j.getGcTimeMillis())
                    .build();
        }

        ServiceMetricsDto.SystemMetrics system = null;
        if (grpcMetrics.hasSystem()) {
            var s = grpcMetrics.getSystem();
            system = ServiceMetricsDto.SystemMetrics.builder()
                    .cpuUsage(s.getSystemCpuUsage())
                    .cpuCount(s.getAvailableProcessors())
                    .build();
        }

        Map<String, Object> rawMetrics = new LinkedHashMap<>();
        rawMetrics.putAll(grpcMetrics.getCustomMetricsMap());

        return ServiceMetricsDto.builder()
                .serviceName(serviceName)
                .timestamp(grpcMetrics.getTimestamp() > 0 
                        ? LocalDateTime.ofInstant(Instant.ofEpochMilli(grpcMetrics.getTimestamp()), ZoneId.systemDefault())
                        : LocalDateTime.now())
                .available(true)
                .memory(memory)
                .jvm(jvm)
                .system(system)
                .rawMetrics(rawMetrics)
                .build();
    }

    /**
     * Add metrics to history (bounded circular buffer).
     */
    private void addMetricsToHistory(String serviceName, ServiceMetricsDto metrics) {
        List<ServiceMetricsDto> history = metricsHistory.computeIfAbsent(serviceName, k -> new CopyOnWriteArrayList<>());
        history.add(metrics);

        // Trim to max size
        while (history.size() > MAX_HISTORY_SIZE) {
            history.remove(0);
        }
    }

    // ==================== Public API Methods ====================

    /**
     * Get dashboard summary with service health status.
     */
    public DashboardSummaryDto getDashboardSummary() {
        List<ServiceHealthDto> serviceHealthList = new ArrayList<>(healthCache.values());
        
        int healthyCount = 0;
        int unhealthyCount = 0;
        int unknownCount = 0;

        for (ServiceHealthDto health : serviceHealthList) {
            switch (health.getStatus()) {
                case "UP" -> healthyCount++;
                case "DOWN" -> unhealthyCount++;
                default -> unknownCount++;
            }
        }

        return DashboardSummaryDto.builder()
                .timestamp(LocalDateTime.now())
                .totalServices(gatewayProperties.getServices().size())
                .healthyServices(healthyCount)
                .unhealthyServices(unhealthyCount)
                .unknownServices(unknownCount)
                .services(serviceHealthList)
                .build();
    }

    /**
     * Get health status for all services.
     */
    public List<ServiceHealthDto> getAllServicesHealth() {
        return new ArrayList<>(healthCache.values());
    }

    /**
     * Get health for a specific service.
     */
    public Optional<ServiceHealthDto> getServiceHealth(String serviceName) {
        return Optional.ofNullable(healthCache.get(serviceName));
    }

    /**
     * Get service info via gRPC.
     */
    public ServiceInfoDto getServiceInfo(String serviceName) {
        GatewayProperties.ServiceConfig config = gatewayProperties.getServices().get(serviceName);
        
        if (config == null) {
            return ServiceInfoDto.builder()
                    .serviceName(serviceName)
                    .available(false)
                    .errorMessage("Service not configured")
                    .build();
        }

        try {
            InfoResponse grpcInfo = grpcHealthClient.getInfo(serviceName);
            
            Map<String, Object> additionalInfo = new LinkedHashMap<>();
            additionalInfo.put("host", config.getHost());
            additionalInfo.put("grpcPort", config.getPort());
            additionalInfo.put("httpPort", config.getHttpPort());
            additionalInfo.putAll(grpcInfo.getAdditionalInfoMap());

            Map<String, Object> gitInfo = null;
            if (grpcInfo.hasGit()) {
                gitInfo = new LinkedHashMap<>();
                gitInfo.put("branch", grpcInfo.getGit().getBranch());
                gitInfo.put("commitId", grpcInfo.getGit().getCommitId());
            }

            return ServiceInfoDto.builder()
                    .serviceName(serviceName)
                    .appName(grpcInfo.getServiceName())
                    .version(grpcInfo.getVersion())
                    .javaVersion(grpcInfo.getJavaVersion())
                    .springBootVersion(grpcInfo.getSpringBootVersion())
                    .gitInfo(gitInfo)
                    .additionalInfo(additionalInfo)
                    .available(true)
                    .build();
        } catch (Exception e) {
            return ServiceInfoDto.builder()
                    .serviceName(serviceName)
                    .available(false)
                    .errorMessage("Could not fetch info via gRPC: " + e.getMessage())
                    .additionalInfo(Map.of(
                            "host", config.getHost(),
                            "grpcPort", config.getPort(),
                            "httpPort", config.getHttpPort()
                    ))
                    .build();
        }
    }

    /**
     * Get metrics for a specific service (live via gRPC).
     */
    public ServiceMetricsDto getServiceMetrics(String serviceName) {
        GatewayProperties.ServiceConfig config = gatewayProperties.getServices().get(serviceName);
        if (config == null) {
            return ServiceMetricsDto.builder()
                    .serviceName(serviceName)
                    .timestamp(LocalDateTime.now())
                    .available(false)
                    .errorMessage("Service not configured")
                    .build();
        }

        try {
            MetricsResponse grpcMetrics = grpcHealthClient.getMetrics(serviceName);
            return convertMetricsResponse(serviceName, grpcMetrics);
        } catch (Exception e) {
            return ServiceMetricsDto.builder()
                    .serviceName(serviceName)
                    .timestamp(LocalDateTime.now())
                    .available(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    /**
     * Get metrics history for a specific service.
     */
    public List<ServiceMetricsDto> getServiceMetricsHistory(String serviceName) {
        return metricsHistory.getOrDefault(serviceName, Collections.emptyList());
    }

    /**
     * Get route statistics.
     */
    public Map<String, Object> getRouteStats() {
        return Map.of(
                "totalRoutes", routeRegistry.getRouteCount(),
                "totalServices", routeRegistry.getServiceCount(),
                "configSource", "static (application.properties)",
                "healthCheckMethod", "gRPC"
        );
    }

    /**
     * Force immediate health check (for manual refresh).
     */
    public void forceHealthCheck() {
        checkAllServicesHealth();
    }
}
