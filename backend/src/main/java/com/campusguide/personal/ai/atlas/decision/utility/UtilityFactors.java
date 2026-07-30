package com.campusguide.personal.ai.atlas.decision.utility;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Encapsulates the core factors influencing normalized utility computation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UtilityFactors implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private double confidence = 0.5;

    @Builder.Default
    private double relevance = 0.5;

    @Builder.Default
    private double urgency = 0.5;

    @Builder.Default
    private double importance = 0.5;

    @Builder.Default
    private double policyAlignment = 1.0;

    @Builder.Default
    private double userBenefit = 0.5;

    public static UtilityFactors defaultFactors() {
        return UtilityFactors.builder()
                .confidence(0.5)
                .relevance(0.5)
                .urgency(0.5)
                .importance(0.5)
                .policyAlignment(1.0)
                .userBenefit(0.5)
                .build();
    }
}
