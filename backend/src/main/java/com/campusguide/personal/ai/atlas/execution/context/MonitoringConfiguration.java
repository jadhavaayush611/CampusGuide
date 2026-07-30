package com.campusguide.personal.ai.atlas.execution.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Monitoring configuration for observability during workflow execution.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonitoringConfiguration implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private boolean metricsEnabled = true;

    @Builder.Default
    private boolean tracingEnabled = true;

    @Builder.Default
    private String logLevel = "INFO";

    @Builder.Default
    private double sampleRate = 1.0;

    @Builder.Default
    private Map<String, Double> alertThresholds = new HashMap<>();

    public static MonitoringConfiguration defaultConfig() {
        return MonitoringConfiguration.builder()
                .metricsEnabled(true)
                .tracingEnabled(true)
                .logLevel("INFO")
                .sampleRate(1.0)
                .build();
    }
}
