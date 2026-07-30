package com.campusguide.personal.ai.atlas.decision.utility;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Normalized utility score (0.0 - 1.0) along with detailed factor breakdown.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UtilityScore implements Serializable {

    private static final long serialVersionUID = 1L;

    private String candidateId;
    private double totalUtility;
    private double normalizedUtility;

    @Builder.Default
    private Map<String, Double> factorScores = new ConcurrentHashMap<>();

    public static UtilityScore zero(String candidateId) {
        return UtilityScore.builder()
                .candidateId(candidateId)
                .totalUtility(0.0)
                .normalizedUtility(0.0)
                .factorScores(Map.of())
                .build();
    }
}
