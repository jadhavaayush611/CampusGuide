package com.campusguide.personal.ai.atlas.decision.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Encapsulates a candidate action option generated for a DecisionContext.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DecisionCandidate implements Serializable {

    private static final long serialVersionUID = 1L;

    private String candidateId;
    private String actionType;
    private String description;
    
    @Builder.Default
    private Map<String, Object> parameters = new ConcurrentHashMap<>();

    private double estimatedUtility;
    private double confidenceScore;
    private double feasibilityScore;
    
    private String rationale;
    private String sourceStrategy;

    @Builder.Default
    private Map<String, Object> metadata = new ConcurrentHashMap<>();

    public void addParameter(String key, Object value) {
        if (key != null && value != null) {
            parameters.put(key, value);
        }
    }

    public static DecisionCandidate simple(String candidateId, String actionType, String description, double confidence) {
        return DecisionCandidate.builder()
                .candidateId(candidateId)
                .actionType(actionType)
                .description(description)
                .confidenceScore(confidence)
                .feasibilityScore(1.0)
                .estimatedUtility(confidence)
                .rationale("Generated candidate " + candidateId)
                .sourceStrategy("DefaultStrategy")
                .parameters(Collections.emptyMap())
                .metadata(Collections.emptyMap())
                .build();
    }
}
