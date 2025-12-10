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
public class ServiceInfoDto {
    
    @JsonProperty("service_name")
    private String serviceName;
    
    @JsonProperty("app_name")
    private String appName;
    
    private String version;
    
    @JsonProperty("java_version")
    private String javaVersion;
    
    @JsonProperty("spring_boot_version")
    private String springBootVersion;
    
    @JsonProperty("build_time")
    private LocalDateTime buildTime;
    
    @JsonProperty("git_info")
    private Map<String, Object> gitInfo;
    
    @JsonProperty("additional_info")
    private Map<String, Object> additionalInfo;
    
    private boolean available;
    
    @JsonProperty("error_message")
    private String errorMessage;
}
