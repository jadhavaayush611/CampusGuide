package com.campusguide.personal.ai.atlas.execution.risk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Encapsulates evaluated risk score and risk level classification.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskScore implements Serializable {

    private static final long serialVersionUID = 1L;

    private double compositeScore;
    private RiskLevel level;

    public enum RiskLevel {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    public static RiskScore fromScore(double score) {
        RiskLevel level;
        if (score < 0.25) {
            level = RiskLevel.LOW;
        } else if (score < 0.55) {
            level = RiskLevel.MEDIUM;
        } else if (score < 0.80) {
            level = RiskLevel.HIGH;
        } else {
            level = RiskLevel.CRITICAL;
        }
        return new RiskScore(score, level);
    }
}
