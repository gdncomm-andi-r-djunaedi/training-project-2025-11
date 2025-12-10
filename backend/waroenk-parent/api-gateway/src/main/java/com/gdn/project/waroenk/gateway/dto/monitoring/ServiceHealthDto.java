package com.gdn.project.waroenk.gateway.dto.monitoring;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ServiceHealthDto {
    
    @JsonProperty("service_name")
    private String serviceName;
    
    private String host;
    
    @JsonProperty("grpc_port")
    private int grpcPort;
    
    @JsonProperty("http_port")
    private int httpPort;
    
    private String status;  // UP, DOWN, UNKNOWN
    
    private String source;  // static, dynamic
    
    @JsonProperty("last_check")
    private LocalDateTime lastCheck;
    
    @JsonProperty("health_details")
    private Map<String, Object> healthDetails;
    
    @JsonProperty("error_message")
    private String errorMessage;
}
