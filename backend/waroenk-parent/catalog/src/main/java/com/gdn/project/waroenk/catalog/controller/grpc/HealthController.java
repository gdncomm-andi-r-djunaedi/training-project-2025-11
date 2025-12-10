package com.gdn.project.waroenk.catalog.controller.grpc;

import com.gdn.project.waroenk.common.*;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.actuate.health.CompositeHealth;

import java.lang.management.*;

/**
 * gRPC Health Service implementation for centralized monitoring.
 * Exposes JVM metrics, memory stats, and health status via gRPC.
 */
@Slf4j
@GrpcService
@RequiredArgsConstructor
public class HealthController extends HealthServiceGrpc.HealthServiceImplBase {

    @Value("${spring.application.name:catalog}")
    private String serviceName;

    @Value("${info.app.version:unknown}")
    private String version;

    private final HealthEndpoint healthEndpoint;

    @Override
    public void getHealth(Empty request, StreamObserver<HealthResponse> responseObserver) {
        try {
            HealthComponent health = healthEndpoint.health();
            HealthStatus status = mapHealthStatus(health.getStatus());

            HealthResponse.Builder builder = HealthResponse.newBuilder()
                    .setStatus(status)
                    .setServiceName(serviceName)
                    .setTimestamp(System.currentTimeMillis());

            // Add component health if this is a composite health
            if (health instanceof CompositeHealth compositeHealth) {
                compositeHealth.getComponents().forEach((name, component) -> {
                    ComponentHealth.Builder compBuilder = ComponentHealth.newBuilder()
                            .setStatus(mapHealthStatus(component.getStatus()));
                    
                    // Extract details if it's a Health instance
                    if (component instanceof Health healthDetails) {
                        healthDetails.getDetails().forEach((k, v) -> 
                            compBuilder.putDetails(k, String.valueOf(v)));
                    }
                    builder.putComponents(name, compBuilder.build());
                });
            }

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error getting health", e);
            responseObserver.onNext(HealthResponse.newBuilder()
                    .setStatus(HealthStatus.DOWN)
                    .setServiceName(serviceName)
                    .setTimestamp(System.currentTimeMillis())
                    .setErrorMessage(e.getMessage())
                    .build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getMetrics(Empty request, StreamObserver<MetricsResponse> responseObserver) {
        try {
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
            RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            ClassLoadingMXBean classBean = ManagementFactory.getClassLoadingMXBean();

            // Memory metrics
            MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
            MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();

            MemoryMetrics memory = MemoryMetrics.newBuilder()
                    .setHeapUsed(heapUsage.getUsed())
                    .setHeapMax(heapUsage.getMax())
                    .setHeapCommitted(heapUsage.getCommitted())
                    .setNonHeapUsed(nonHeapUsage.getUsed())
                    .setNonHeapCommitted(nonHeapUsage.getCommitted())
                    .setHeapUsedPercentage(heapUsage.getMax() > 0 
                            ? (double) heapUsage.getUsed() / heapUsage.getMax() * 100 : 0)
                    .build();

            // JVM metrics
            JvmMetrics.Builder jvmBuilder = JvmMetrics.newBuilder()
                    .setThreadCount(threadBean.getThreadCount())
                    .setThreadPeakCount(threadBean.getPeakThreadCount())
                    .setThreadDaemonCount(threadBean.getDaemonThreadCount())
                    .setClassesLoaded(classBean.getLoadedClassCount())
                    .setUptimeSeconds(runtimeBean.getUptime() / 1000);

            // CPU usage (if available)
            if (osBean instanceof com.sun.management.OperatingSystemMXBean sunOsBean) {
                double processCpu = sunOsBean.getProcessCpuLoad();
                // Value is -1 if not available, otherwise 0.0 to 1.0
                if (processCpu >= 0) {
                    jvmBuilder.setCpuUsage(processCpu * 100);
                } else {
                    jvmBuilder.setCpuUsage(0);
                }
            }

            // GC metrics
            long gcCount = 0;
            long gcTime = 0;
            for (GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
                long count = gcBean.getCollectionCount();
                long time = gcBean.getCollectionTime();
                if (count >= 0) gcCount += count;
                if (time >= 0) gcTime += time;
            }
            jvmBuilder.setGcCount(gcCount);
            jvmBuilder.setGcTimeMillis(gcTime);

            // System metrics
            SystemMetrics.Builder systemBuilder = SystemMetrics.newBuilder()
                    .setAvailableProcessors(osBean.getAvailableProcessors());

            if (osBean instanceof com.sun.management.OperatingSystemMXBean sunOsBean) {
                double systemCpu = sunOsBean.getCpuLoad();
                // Value is -1 if not available, otherwise 0.0 to 1.0
                if (systemCpu >= 0) {
                    systemBuilder.setSystemCpuUsage(systemCpu * 100);
                } else {
                    systemBuilder.setSystemCpuUsage(0);
                }
                systemBuilder.setTotalMemory(sunOsBean.getTotalMemorySize());
                systemBuilder.setFreeMemory(sunOsBean.getFreeMemorySize());
            }

            MetricsResponse response = MetricsResponse.newBuilder()
                    .setServiceName(serviceName)
                    .setTimestamp(System.currentTimeMillis())
                    .setMemory(memory)
                    .setJvm(jvmBuilder.build())
                    .setSystem(systemBuilder.build())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error getting metrics", e);
            responseObserver.onNext(MetricsResponse.newBuilder()
                    .setServiceName(serviceName)
                    .setTimestamp(System.currentTimeMillis())
                    .build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getInfo(Empty request, StreamObserver<InfoResponse> responseObserver) {
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();

        InfoResponse response = InfoResponse.newBuilder()
                .setServiceName(serviceName)
                .setVersion(version)
                .setJavaVersion(System.getProperty("java.version", "unknown"))
                .putAdditionalInfo("java.vendor", System.getProperty("java.vendor", "unknown"))
                .putAdditionalInfo("os.name", System.getProperty("os.name", "unknown"))
                .putAdditionalInfo("os.arch", System.getProperty("os.arch", "unknown"))
                .putAdditionalInfo("jvm.name", runtimeBean.getVmName())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private HealthStatus mapHealthStatus(Status status) {
        if (Status.UP.equals(status)) {
            return HealthStatus.UP;
        } else if (Status.DOWN.equals(status)) {
            return HealthStatus.DOWN;
        } else if (Status.OUT_OF_SERVICE.equals(status)) {
            return HealthStatus.DOWN;
        } else {
            return HealthStatus.UNKNOWN;
        }
    }
}
