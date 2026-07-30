package com.campusguide.personal.ai.atlas.decision.explanation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Structured decision reason element with code, narrative, and impact level.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DecisionReason implements Serializable {

    private static final long serialVersionUID = 1L;

    private String reasonCode;
    private String narrative;
    private ImpactLevel impactLevel;

    public enum ImpactLevel {
        LOW,
        MEDIUM,
        HIGH,
        DECISIVE
    }

    public static DecisionReason decisive(String code, String narrative) {
        return DecisionReason.builder()
                .reasonCode(code)
                .narrative(narrative)
                .impactLevel(ImpactLevel.DECISIVE)
                .build();
    }
}
