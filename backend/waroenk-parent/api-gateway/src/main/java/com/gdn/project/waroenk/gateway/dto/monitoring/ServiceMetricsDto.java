package com.gdn.project.waroenk.gateway.dto.monitoring;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceMetricsDto {
    
    @JsonProperty("service_name")
    private String serviceName;
    
    private LocalDateTime timestamp;
    
    private boolean available;
    
    @JsonProperty("error_message")
    private String errorMessage;
    
    // Memory metrics
    private MemoryMetrics memory;
    
    // JVM metrics
    private JvmMetrics jvm;
    
    // HTTP metrics
    private HttpMetrics http;
    
    // System metrics
    private SystemMetrics system;
    
    // Raw metrics (for custom display)
    @JsonProperty("raw_metrics")
    private Map<String, Object> rawMetrics;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemoryMetrics {
        @JsonProperty("heap_used")
        private Long heapUsed;
        
        @JsonProperty("heap_max")
        private Long heapMax;
        
        @JsonProperty("heap_committed")
        private Long heapCommitted;
        
        @JsonProperty("non_heap_used")
        private Long nonHeapUsed;
        
        @JsonProperty("non_heap_committed")
        private Long nonHeapCommitted;
        
        @JsonProperty("heap_used_percentage")
        private Double heapUsedPercentage;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JvmMetrics {
        @JsonProperty("thread_count")
        private Integer threadCount;
        
        @JsonProperty("thread_peak_count")
        private Integer threadPeakCount;
        
        @JsonProperty("thread_daemon_count")
        private Integer threadDaemonCount;
        
        @JsonProperty("classes_loaded")
        private Long classesLoaded;
        
        @JsonProperty("gc_pause_count")
        private Long gcPauseCount;
        
        @JsonProperty("gc_pause_time")
        private Double gcPauseTime;
        
        @JsonProperty("cpu_usage")
        private Double cpuUsage;
        
        @JsonProperty("uptime_seconds")
        private Long uptimeSeconds;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HttpMetrics {
        @JsonProperty("total_requests")
        private Long totalRequests;
        
        @JsonProperty("requests_per_second")
        private Double requestsPerSecond;
        
        @JsonProperty("avg_response_time")
        private Double avgResponseTime;
        
        @JsonProperty("error_count")
        private Long errorCount;
        
        @JsonProperty("requests_by_status")
        private Map<String, Long> requestsByStatus;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SystemMetrics {
        @JsonProperty("cpu_usage")
        private Double cpuUsage;
        
        @JsonProperty("cpu_count")
        private Integer cpuCount;
        
        @JsonProperty("disk_free_space")
        private Long diskFreeSpace;
        
        @JsonProperty("disk_total_space")
        private Long diskTotalSpace;
    }
}
