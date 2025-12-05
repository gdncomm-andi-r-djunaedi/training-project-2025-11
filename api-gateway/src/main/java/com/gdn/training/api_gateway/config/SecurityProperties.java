package com.gdn.training.api_gateway.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

    private List<String> publicPaths = new ArrayList<>();
}

