package com.campusguide.personal.ai.atlas.decision.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Operational and safety constraints governing decision evaluation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DecisionConstraints implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private long maxLatencyMs = 2000L;

    @Builder.Default
    private double minConfidence = 0.50;

    @Builder.Default
    private int maxCandidates = 10;

    @Builder.Default
    private Set<String> requiredPermissions = Collections.emptySet();

    @Builder.Default
    private long timeWindowSeconds = 3600L;

    @Builder.Default
    private String safetyLevel = "STRICT";

    @Builder.Default
    private Map<String, Object> customConstraints = new ConcurrentHashMap<>();

    public static DecisionConstraints defaultConstraints() {
        return DecisionConstraints.builder()
                .maxLatencyMs(2000L)
                .minConfidence(0.50)
                .maxCandidates(10)
                .requiredPermissions(Collections.emptySet())
                .timeWindowSeconds(3600L)
                .safetyLevel("STRICT")
                .customConstraints(Collections.emptyMap())
                .build();
    }
}
