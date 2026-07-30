package com.campusguide.personal.ai.atlas.planning.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Audit and execution-independent metadata for an ExecutionPlan.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanningMetadata implements Serializable {

    private static final long serialVersionUID = 1L;

    private String planId;
    private String traceId;
    private String version;
    private String generatorId;
    private String strategyUsed;
    private String environment;

    @Builder.Default
    private Instant createdAt = Instant.now();

    @Builder.Default
    private Instant updatedAt = Instant.now();

    @Builder.Default
    private Map<String, Object> attributes = new ConcurrentHashMap<>();

    public static PlanningMetadata createDefault(String traceId, String strategyUsed) {
        return PlanningMetadata.builder()
                .traceId(traceId != null ? traceId : "trace_default")
                .version("1.0.0")
                .generatorId("AtlasPlanningEngine_v1")
                .strategyUsed(strategyUsed != null ? strategyUsed : "DEFAULT")
                .environment("PRODUCTION")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
