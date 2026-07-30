package com.campusguide.personal.ai.atlas.decision.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Metadata container for decision tracking, auditing, and observability.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DecisionMetadata implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private String version = "1.0.0";

    private String generatorId;
    private String evaluatorId;
    private String traceId;
    private String environment;

    @Builder.Default
    private Instant createdAt = Instant.now();

    @Builder.Default
    private Map<String, String> customTags = new ConcurrentHashMap<>();

    public static DecisionMetadata createDefault(String traceId) {
        return DecisionMetadata.builder()
                .version("1.0.0")
                .generatorId("default-candidate-generator")
                .evaluatorId("default-decision-evaluator")
                .traceId(traceId != null ? traceId : "trace-" + System.currentTimeMillis())
                .environment("production")
                .createdAt(Instant.now())
                .customTags(Collections.emptyMap())
                .build();
    }
}
