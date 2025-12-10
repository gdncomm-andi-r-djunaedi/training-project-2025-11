package com.gdn.project.waroenk.gateway.dto.monitoring;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDto {
    
    private LocalDateTime timestamp;
    
    @JsonProperty("total_services")
    private int totalServices;
    
    @JsonProperty("healthy_services")
    private int healthyServices;
    
    @JsonProperty("unhealthy_services")
    private int unhealthyServices;
    
    @JsonProperty("unknown_services")
    private int unknownServices;
    
    private List<ServiceHealthDto> services;
}
