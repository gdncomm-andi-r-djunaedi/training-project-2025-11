package com.gdn.project.waroenk.gateway.config;

import com.gdn.project.waroenk.gateway.service.StaticRouteRegistry;
import com.gdn.project.waroenk.gateway.service.StaticRouteRegistry.ServiceInfo;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Configuration for managing gRPC channels to backend services.
 * Creates channels lazily for services defined in static configuration.
 * 
 * Memory optimized:
 * - Channels are created lazily on first use
 * - Idle timeout releases unused connections
 * - No persistent keep-alive without active calls
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class GrpcChannelConfig {

    private final GatewayProperties gatewayProperties;
    private final Map<String, ManagedChannel> channels = new ConcurrentHashMap<>();

    // Injected after StaticRouteRegistry is created to avoid circular dependency
    private StaticRouteRegistry routeRegistry;

    public void setRouteRegistry(StaticRouteRegistry routeRegistry) {
        this.routeRegistry = routeRegistry;
    }

    @PostConstruct
    public void init() {
        log.info("GrpcChannelConfig initialized - {} services configured", 
                gatewayProperties.getServices().size());
        
        // Log configured services (channels created lazily)
        gatewayProperties.getServices().forEach((name, config) -> {
            log.info("  Service configured: {} -> {}:{} (TLS: {})",
                    name, config.getHost(), config.getPort(), config.isUseTls());
        });
    }

    @PreDestroy
    public void shutdownChannels() {
        log.info("Shutting down {} gRPC channels...", channels.size());
        channels.forEach((name, channel) -> {
            try {
                if (!channel.isShutdown()) {
                    channel.shutdown();
                    // Don't block for too long during shutdown
                    if (!channel.awaitTermination(2, TimeUnit.SECONDS)) {
                        channel.shutdownNow();
                    }
                }
            } catch (InterruptedException e) {
                channel.shutdownNow();
                Thread.currentThread().interrupt();
            }
        });
        channels.clear();
        log.info("All gRPC channels shut down");
    }

    /**
     * Get the channel for a specific service.
     * Creates channel lazily if not exists.
     */
    public ManagedChannel getChannel(String serviceName) {
        return channels.computeIfAbsent(serviceName, this::createChannelForService);
    }

    /**
     * Create a channel for a service using static configuration.
     */
    private ManagedChannel createChannelForService(String serviceName) {
        // First try from gateway properties
        GatewayProperties.ServiceConfig config = gatewayProperties.getServices().get(serviceName);
        if (config != null) {
            return createChannel(serviceName, config.getHost(), config.getPort(), config.isUseTls());
        }

        // Try from route registry if available
        if (routeRegistry != null) {
            Optional<ServiceInfo> serviceInfo = routeRegistry.getServiceInfo(serviceName);
            if (serviceInfo.isPresent()) {
                ServiceInfo info = serviceInfo.get();
                return createChannel(serviceName, info.host(), info.port(), info.useTls());
            }
        }

        throw new IllegalArgumentException("No configuration found for service: " + serviceName);
    }

    /**
     * Create a channel for a service.
     * Optimized for stability and resource efficiency:
     * - Disable keepAliveWithoutCalls to release idle connections
     * - Add idle timeout to prevent connection buildup
     * - Limit max retry attempts
     */
    private ManagedChannel createChannel(String serviceName, String host, int port, boolean useTls) {
        ManagedChannelBuilder<?> builder = ManagedChannelBuilder
                .forAddress(host, port)
                // Keep-alive settings (only when there ARE active calls)
                .keepAliveTime(60, TimeUnit.SECONDS)
                .keepAliveTimeout(20, TimeUnit.SECONDS)
                .keepAliveWithoutCalls(false) // CRITICAL: Don't keep alive without calls!
                // Idle timeout - close connections after 5 min of inactivity
                .idleTimeout(5, TimeUnit.MINUTES)
                // Limit resources
                .maxInboundMessageSize(16 * 1024 * 1024) // 16MB max message
                .maxRetryAttempts(3);

        if (!useTls) {
            builder.usePlaintext();
        }

        ManagedChannel channel = builder.build();
        log.info("Created gRPC channel for service: {} -> {}:{} (TLS: {})",
                serviceName, host, port, useTls);

        return channel;
    }

    /**
     * Check if a channel exists and is active for the given service.
     */
    public boolean hasChannel(String serviceName) {
        ManagedChannel channel = channels.get(serviceName);
        return channel != null && !channel.isShutdown() && !channel.isTerminated();
    }

    /**
     * Get all service names that have active channels.
     */
    public Set<String> getActiveServiceNames() {
        return channels.keySet();
    }

    /**
     * Get all configured service names.
     */
    public Set<String> getConfiguredServiceNames() {
        return gatewayProperties.getServices().keySet();
    }
}
