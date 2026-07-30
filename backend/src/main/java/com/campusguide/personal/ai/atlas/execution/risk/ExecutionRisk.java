package com.campusguide.personal.ai.atlas.execution.risk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Aggregate risk assessment for an ExecutableWorkflow.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutionRisk implements Serializable {

    private static final long serialVersionUID = 1L;

    private String assessmentId;
    private double overallRiskScore;
    private RiskScore.RiskLevel riskCategory;

    @Builder.Default
    private List<RiskFactor> factors = new ArrayList<>();

    private double failureProbability;

    @Builder.Default
    private List<String> recommendations = new ArrayList<>();

    public static ExecutionRisk lowRisk() {
        return ExecutionRisk.builder()
                .assessmentId("risk_low")
                .overallRiskScore(0.1)
                .riskCategory(RiskScore.RiskLevel.LOW)
                .failureProbability(0.05)
                .factors(new ArrayList<>())
                .recommendations(new ArrayList<>())
                .build();
    }
}
