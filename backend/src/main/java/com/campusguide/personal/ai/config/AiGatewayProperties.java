package com.campusguide.personal.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "ai.gateway")
@Data
public class AiGatewayProperties {
    private String baseUrl = "http://localhost:8000";
    private Duration timeout = Duration.ofSeconds(10);
    private boolean enabled = true;
    private int historyLimit = 20;
}
