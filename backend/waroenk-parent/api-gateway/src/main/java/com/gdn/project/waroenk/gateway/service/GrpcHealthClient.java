package com.gdn.project.waroenk.gateway.service;

import com.gdn.project.waroenk.common.*;
import com.gdn.project.waroenk.gateway.config.GrpcChannelConfig;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * gRPC client for health and metrics collection from backend services.
 * Uses existing gRPC channels from GrpcChannelConfig.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GrpcHealthClient {

    private final GrpcChannelConfig channelConfig;

    // Timeout for health/metrics calls
    private static final long TIMEOUT_SECONDS = 5;

    /**
     * Get health status from a service via gRPC.
     */
    public HealthResponse getHealth(String serviceName) {
        try {
            ManagedChannel channel = channelConfig.getChannel(serviceName);
            HealthServiceGrpc.HealthServiceBlockingStub stub = HealthServiceGrpc.newBlockingStub(channel)
                    .withDeadlineAfter(TIMEOUT_SECONDS, TimeUnit.SECONDS);

            return stub.getHealth(Empty.newBuilder().build());
        } catch (StatusRuntimeException e) {
            log.warn("gRPC health check failed for {}: {}", serviceName, e.getStatus());
            return HealthResponse.newBuilder()
                    .setStatus(HealthStatus.DOWN)
                    .setServiceName(serviceName)
                    .setTimestamp(System.currentTimeMillis())
                    .setErrorMessage("gRPC error: " + e.getStatus().getDescription())
                    .build();
        } catch (Exception e) {
            log.warn("Health check failed for {}: {}", serviceName, e.getMessage());
            return HealthResponse.newBuilder()
                    .setStatus(HealthStatus.DOWN)
                    .setServiceName(serviceName)
                    .setTimestamp(System.currentTimeMillis())
                    .setErrorMessage(e.getMessage())
                    .build();
        }
    }

    /**
     * Get metrics from a service via gRPC.
     */
    public MetricsResponse getMetrics(String serviceName) {
        try {
            ManagedChannel channel = channelConfig.getChannel(serviceName);
            HealthServiceGrpc.HealthServiceBlockingStub stub = HealthServiceGrpc.newBlockingStub(channel)
                    .withDeadlineAfter(TIMEOUT_SECONDS, TimeUnit.SECONDS);

            return stub.getMetrics(Empty.newBuilder().build());
        } catch (StatusRuntimeException e) {
            log.warn("gRPC metrics fetch failed for {}: {}", serviceName, e.getStatus());
            return MetricsResponse.newBuilder()
                    .setServiceName(serviceName)
                    .setTimestamp(System.currentTimeMillis())
                    .build();
        } catch (Exception e) {
            log.warn("Metrics fetch failed for {}: {}", serviceName, e.getMessage());
            return MetricsResponse.newBuilder()
                    .setServiceName(serviceName)
                    .setTimestamp(System.currentTimeMillis())
                    .build();
        }
    }

    /**
     * Get service info via gRPC.
     */
    public InfoResponse getInfo(String serviceName) {
        try {
            ManagedChannel channel = channelConfig.getChannel(serviceName);
            HealthServiceGrpc.HealthServiceBlockingStub stub = HealthServiceGrpc.newBlockingStub(channel)
                    .withDeadlineAfter(TIMEOUT_SECONDS, TimeUnit.SECONDS);

            return stub.getInfo(Empty.newBuilder().build());
        } catch (StatusRuntimeException e) {
            log.warn("gRPC info fetch failed for {}: {}", serviceName, e.getStatus());
            return InfoResponse.newBuilder()
                    .setServiceName(serviceName)
                    .build();
        } catch (Exception e) {
            log.warn("Info fetch failed for {}: {}", serviceName, e.getMessage());
            return InfoResponse.newBuilder()
                    .setServiceName(serviceName)
                    .build();
        }
    }
}


